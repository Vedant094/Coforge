package com.hackathon.kiosk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.kiosk.dto.GeminiParsedResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Live NLU provider - calls the real Gemini API over the network.
 * Wrapped by {@link ResilientNluService}, which is what the rest of the
 * app actually talks to; this class never needs to know about mocking.
 */
@Service
@Qualifier("live")
public class GeminiService implements NluInterpreter {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${kiosk.ai.live.timeout-ms:4000}")
    private long timeoutMs;

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper mapper = new ObjectMapper();

    // very small in-memory context store: sessionId -> last few turns
    private final Map<String, List<String>> sessionHistory = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
        You are an AI assistant at an airport self-service kiosk. Passengers speak
        to you in any language. Your job in ONE response:

        1. Detect the language of the passenger's message (ISO 639-1 code, e.g. "hi", "es", "en").
        2. Classify the intent into EXACTLY one of:
           - "check_in"
           - "seat_selection"
           - "baggage"
           - "flight_status"
           - "navigation"
           - "greeting"
           - "unknown"
        3. Extract any entities mentioned (flight number, PNR/booking ref,
           passenger name, seat preference, number of bags, destination gate/terminal).
           Leave a field empty string "" if not mentioned.
        4. Write a short, warm, clear reply IN THE DETECTED LANGUAGE. Keep it under
           35 words - this is read aloud by text-to-speech and shown on a kiosk screen.
        5. If required info is missing to proceed (e.g. no PNR for check-in), set
           needs_followup to true and put ONE clarifying question in followup_question,
           also in the detected language.

        Respond with ONLY valid JSON, no markdown fences, no extra text, matching
        exactly this schema:

        {
          "detected_language": "string (ISO 639-1 code)",
          "language_name": "string (e.g. Hindi, Spanish, English)",
          "intent": "string (one of the categories above)",
          "entities": {
            "flight_number": "string",
            "pnr": "string",
            "passenger_name": "string",
            "seat_preference": "string",
            "bag_count": "string",
            "destination": "string"
          },
          "reply_text": "string (in detected language)",
          "needs_followup": true,
          "followup_question": "string (in detected language, empty if needs_followup is false)"
        }

        Conversation so far:
        %s

        Passenger message:
        %s
        """;

    @Override
    public GeminiParsedResponse interpret(String sessionId, String userMessage) {
        List<String> history = sessionHistory.computeIfAbsent(sessionId, k -> new java.util.ArrayList<>());
        String historyText = String.join("\n", history);
        String fullPrompt = SYSTEM_PROMPT.formatted(historyText, userMessage);

        Map<String, Object> body = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", fullPrompt)))
            ),
            "generationConfig", Map.of(
                "temperature", 0.3,
                "response_mime_type", "application/json"
            )
        );

        String rawResponse = webClient.post()
            .uri(apiUrl + "?key=" + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofMillis(timeoutMs))
            .block();

        try {
            JsonNode root = mapper.readTree(rawResponse);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            GeminiParsedResponse parsed = mapper.readValue(text, GeminiParsedResponse.class);

            history.add("Passenger: " + userMessage);
            history.add("Assistant: " + parsed.getReplyText());
            if (history.size() > 8) {
                history.subList(0, history.size() - 8).clear();
            }

            return parsed;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + rawResponse, e);
        }
    }
}
