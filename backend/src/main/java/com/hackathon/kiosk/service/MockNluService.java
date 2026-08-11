package com.hackathon.kiosk.service;

import com.hackathon.kiosk.dto.Entities;
import com.hackathon.kiosk.dto.GeminiParsedResponse;
import com.hackathon.kiosk.store.MockDataStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline stand-in for {@link GeminiService}. No network call, no API key,
 * no quota, no rate limit - it can be hit as many times as you like.
 *
 * It does the same job (language detection, intent classification, entity
 * extraction, short reply generation) with simple rules instead of an LLM:
 *
 *  1. Language is guessed from the Unicode script the message is written in.
 *  2. Intent is guessed from a keyword table, checked in each supported language.
 *  3. Entities are pulled out with regexes (flight number, PNR, bag count, ...),
 *     then cross-checked against {@link MockDataStore} where possible so a
 *     recognised flight number also fills in things like destination.
 *  4. The reply is a canned template for the detected intent + language.
 *
 * This intentionally does not try to be a real NLU engine - it is a
 * deterministic, dependency-free double so the kiosk keeps working end to
 * end (demos, integration tests, offline development) when the real Gemini
 * API is unavailable, over quota, or simply not worth burning credits on.
 */
@Service
@Qualifier("mock")
public class MockNluService implements NluInterpreter {

    private final MockDataStore store;

    // Same lightweight in-memory session history the live service keeps,
    // so conversational context behaves the same way regardless of provider.
    private final Map<String, List<String>> sessionHistory = new ConcurrentHashMap<>();

    private static final Pattern FLIGHT_NUMBER = Pattern.compile("\\b([A-Za-z]{2}\\s?-?\\d{2,4})\\b");
    private static final Pattern PNR = Pattern.compile("\\b(?:pnr|booking\\s*ref(?:erence)?|reference)\\D{0,5}([A-Za-z0-9]{5,8})\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BAG_COUNT = Pattern.compile("\\b(\\d{1,2})\\s*(?:bags?|luggage|suitcases?)\\b",
            Pattern.CASE_INSENSITIVE);

    public MockNluService(MockDataStore store) {
        this.store = store;
    }

    @Override
    public GeminiParsedResponse interpret(String sessionId, String userMessage) {
        List<String> history = sessionHistory.computeIfAbsent(sessionId, k -> new java.util.ArrayList<>());

        String message = userMessage == null ? "" : userMessage.trim();
        DetectedLanguage lang = detectLanguage(message);
        String intent = classifyIntent(message, lang.code);
        Entities entities = extractEntities(message);

        GeminiParsedResponse parsed = new GeminiParsedResponse();
        parsed.setDetectedLanguage(lang.code);
        parsed.setLanguageName(lang.name);
        parsed.setIntent(intent);
        parsed.setEntities(entities);

        boolean needsFollowup = false;
        String followup = "";
        String reply;

        switch (intent) {
            case "check_in" -> {
                if (entities.getPnr().isBlank()) {
                    needsFollowup = true;
                    followup = Templates.get(lang.code, "ask_pnr");
                    reply = Templates.get(lang.code, "check_in_ack");
                } else {
                    reply = Templates.get(lang.code, "check_in_ok");
                }
            }
            case "seat_selection" -> {
                if (entities.getPnr().isBlank()) {
                    needsFollowup = true;
                    followup = Templates.get(lang.code, "ask_pnr");
                }
                reply = Templates.get(lang.code, "seat_selection");
            }
            case "baggage" -> reply = Templates.get(lang.code, "baggage");
            case "flight_status" -> {
                if (entities.getFlightNumber().isBlank()) {
                    needsFollowup = true;
                    followup = Templates.get(lang.code, "ask_flight_number");
                }
                reply = Templates.get(lang.code, "flight_status");
            }
            case "greeting" -> reply = Templates.get(lang.code, "greeting");
            default -> {
                reply = Templates.get(lang.code, "unknown");
                needsFollowup = true;
                followup = Templates.get(lang.code, "ask_clarify");
            }
        }

        parsed.setReplyText(reply);
        parsed.setNeedsFollowup(needsFollowup);
        parsed.setFollowupQuestion(followup);

        history.add("Passenger: " + message);
        history.add("Assistant: " + reply);
        if (history.size() > 8) {
            history.subList(0, history.size() - 8).clear();
        }

        return parsed;
    }

    // ---- language detection --------------------------------------------------

    private record DetectedLanguage(String code, String name) {}

    private DetectedLanguage detectLanguage(String text) {
        for (char c : text.toCharArray()) {
            Character.UnicodeScript script = Character.UnicodeScript.of(c);
            switch (script) {
                case DEVANAGARI -> { return new DetectedLanguage("hi", "Hindi"); }
                case ARABIC -> { return new DetectedLanguage("ar", "Arabic"); }
                case HAN -> { return new DetectedLanguage("zh", "Chinese"); }
                case HIRAGANA, KATAKANA -> { return new DetectedLanguage("ja", "Japanese"); }
                case HANGUL -> { return new DetectedLanguage("ko", "Korean"); }
                case CYRILLIC -> { return new DetectedLanguage("ru", "Russian"); }
                case BENGALI -> { return new DetectedLanguage("bn", "Bengali"); }
                case TAMIL -> { return new DetectedLanguage("ta", "Tamil"); }
                default -> { /* keep scanning / fall through to keyword check below */ }
            }
        }
        // Latin script: cheap keyword sniff between the languages we ship templates for.
        String lower = text.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "hola", "gracias", "vuelo", "maleta", "asiento")) {
            return new DetectedLanguage("es", "Spanish");
        }
        if (containsAny(lower, "bonjour", "merci", "vol ", "bagage", "siege")) {
            return new DetectedLanguage("fr", "French");
        }
        return new DetectedLanguage("en", "English");
    }

    private boolean containsAny(String text, String... needles) {
        for (String n : needles) {
            if (text.contains(n)) return true;
        }
        return false;
    }

    // ---- intent classification -------------------------------------------------

    private String classifyIntent(String text, String langCode) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.isBlank()) return "unknown";

        if (containsAny(lower, "hi", "hello", "hey", "namaste", "hola", "bonjour", "good morning", "good evening")
                && lower.split("\\s+").length <= 4) {
            return "greeting";
        }
        if (containsAny(lower, "check in", "check-in", "checkin", "boarding pass", "checkin karna")) {
            return "check_in";
        }
        if (containsAny(lower, "seat", "window seat", "aisle", "asiento", "siege")) {
            return "seat_selection";
        }
        if (containsAny(lower, "bag", "luggage", "baggage", "suitcase", "maleta", "bagage")) {
            return "baggage";
        }
        if (containsAny(lower, "status", "delay", "delayed", "on time", "gate", "departure", "vuelo")) {
            return "flight_status";
        }
        if (containsAny(lower, "where", "how do i get", "direction", "navigate", "terminal")) {
            return "navigation";
        }
        return "unknown";
    }

    // ---- entity extraction -------------------------------------------------

    private Entities extractEntities(String text) {
        Entities e = new Entities();

        Matcher flightMatcher = FLIGHT_NUMBER.matcher(text);
        if (flightMatcher.find()) {
            String candidate = flightMatcher.group(1).replace(" ", "").replace("-", "").toUpperCase(Locale.ROOT);
            e.setFlightNumber(candidate);
            // Cross-check against known mock flights so entity extraction feels
            // "real" - if it matches, fill in destination for free.
            store.findFlightByNumber(candidate).ifPresent(f -> e.setDestination(f.getDestination()));
        }

        Matcher pnrMatcher = PNR.matcher(text);
        if (pnrMatcher.find()) {
            e.setPnr(pnrMatcher.group(1).toUpperCase(Locale.ROOT));
        }

        Matcher bagMatcher = BAG_COUNT.matcher(text);
        if (bagMatcher.find()) {
            e.setBagCount(bagMatcher.group(1));
        }

        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("window")) e.setSeatPreference("window");
        else if (lower.contains("aisle")) e.setSeatPreference("aisle");
        else if (lower.contains("middle")) e.setSeatPreference("middle");

        return e;
    }

    /** Small canned-reply table per intent, per language. Falls back to English. */
    private static final class Templates {
        private static final Map<String, Map<String, String>> BY_LANG = Map.of(
            "en", Map.ofEntries(
                Map.entry("greeting", "Hello! I can help with check-in, seats, baggage, or flight status. What do you need?"),
                Map.entry("check_in_ack", "Happy to check you in."),
                Map.entry("check_in_ok", "You're checked in. Your boarding pass is ready."),
                Map.entry("seat_selection", "Let's find you a seat."),
                Map.entry("baggage", "Here is the baggage allowance for your fare."),
                Map.entry("flight_status", "Here is the latest status for your flight."),
                Map.entry("unknown", "I didn't quite catch that."),
                Map.entry("ask_pnr", "Could you share your booking reference (PNR)?"),
                Map.entry("ask_flight_number", "Which flight number would you like status for?"),
                Map.entry("ask_clarify", "Could you tell me a bit more - check-in, seat, baggage, or flight status?")
            ),
            "hi", Map.ofEntries(
                Map.entry("greeting", "Namaste! Main check-in, seat, baggage ya flight status me madad kar sakta hoon."),
                Map.entry("check_in_ack", "Check-in me madad karta hoon."),
                Map.entry("check_in_ok", "Aapka check-in ho gaya hai. Boarding pass taiyaar hai."),
                Map.entry("seat_selection", "Chaliye aapke liye seat dhoondte hain."),
                Map.entry("baggage", "Yeh raha aapke fare ka baggage allowance."),
                Map.entry("flight_status", "Yeh raha aapki flight ka latest status."),
                Map.entry("unknown", "Maaf kijiye, samajh nahi paya."),
                Map.entry("ask_pnr", "Kripya apna booking reference (PNR) bataiye."),
                Map.entry("ask_flight_number", "Kis flight number ka status chahiye?"),
                Map.entry("ask_clarify", "Kya aap check-in, seat, baggage ya flight status me se batayenge?")
            ),
            "es", Map.ofEntries(
                Map.entry("greeting", "Hola! Puedo ayudar con el check-in, asientos, equipaje o el estado del vuelo."),
                Map.entry("check_in_ack", "Con gusto le ayudo con el check-in."),
                Map.entry("check_in_ok", "Ya hizo el check-in. Su tarjeta de embarque esta lista."),
                Map.entry("seat_selection", "Busquemos un asiento para usted."),
                Map.entry("baggage", "Aqui esta la franquicia de equipaje de su tarifa."),
                Map.entry("flight_status", "Aqui esta el ultimo estado de su vuelo."),
                Map.entry("unknown", "No entendi bien eso."),
                Map.entry("ask_pnr", "Podria compartir su codigo de reserva (PNR)?"),
                Map.entry("ask_flight_number", "Para que numero de vuelo desea el estado?"),
                Map.entry("ask_clarify", "Es sobre check-in, asiento, equipaje o estado del vuelo?")
            )
        );

        static String get(String langCode, String key) {
            Map<String, String> table = BY_LANG.getOrDefault(langCode, BY_LANG.get("en"));
            return table.getOrDefault(key, BY_LANG.get("en").get(key));
        }
    }
}
