package com.hackathon.kiosk.controller;

import com.hackathon.kiosk.dto.ChatRequest;
import com.hackathon.kiosk.dto.ChatResponse;
import com.hackathon.kiosk.dto.GeminiParsedResponse;
import com.hackathon.kiosk.model.Flight;
import com.hackathon.kiosk.service.BaggageService;
import com.hackathon.kiosk.service.CheckInService;
import com.hackathon.kiosk.service.FlightStatusService;
import com.hackathon.kiosk.service.NluInterpreter;
import com.hackathon.kiosk.service.SeatService;
import com.hackathon.kiosk.store.MockDataStore;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final NluInterpreter geminiService;
    private final CheckInService checkInService;
    private final SeatService seatService;
    private final BaggageService baggageService;
    private final FlightStatusService flightStatusService;
    private final MockDataStore store;

    // Spring injects the @Primary NluInterpreter bean here (ResilientNluService),
    // which transparently handles live/mock switching - this class is unchanged.
    public ChatController(NluInterpreter geminiService,
                           CheckInService checkInService,
                           SeatService seatService,
                           BaggageService baggageService,
                           FlightStatusService flightStatusService,
                           MockDataStore store) {
        this.geminiService = geminiService;
        this.checkInService = checkInService;
        this.seatService = seatService;
        this.baggageService = baggageService;
        this.flightStatusService = flightStatusService;
        this.store = store;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String sessionId = (request.getSessionId() == null || request.getSessionId().isBlank())
                ? "default" : request.getSessionId();

        GeminiParsedResponse parsed = geminiService.interpret(sessionId, request.getMessage());

        ChatResponse response = new ChatResponse();
        response.setDetectedLanguage(parsed.getDetectedLanguage());
        response.setLanguageName(parsed.getLanguageName());
        response.setIntent(parsed.getIntent());
        response.setReplyText(parsed.getReplyText());
        response.setNeedsFollowup(parsed.isNeedsFollowup());
        response.setFollowupQuestion(parsed.getFollowupQuestion());

        Map<String, Object> data = new HashMap<>();

        if (!parsed.isNeedsFollowup()) {
            switch (parsed.getIntent()) {
                case "check_in" -> data = checkInService.checkIn(parsed.getEntities().getPnr());
                case "seat_selection" -> {
                    String pnr = parsed.getEntities().getPnr();
                    if (pnr != null && !pnr.isBlank()) {
                        Optional<Flight> flight = store.findFlightByNumber(parsed.getEntities().getFlightNumber());
                        Long flightId = flight.map(Flight::getId).orElse(1L);
                        data = seatService.getAvailableSeats(flightId);
                    }
                }
                case "baggage" -> {
                    String flightNum = parsed.getEntities().getFlightNumber();
                    Optional<Flight> flight = flightNum != null && !flightNum.isBlank()
                            ? store.findFlightByNumber(flightNum)
                            : Optional.empty();
                    String airline = flight.map(Flight::getAirline).orElse("Air India");
                    data = baggageService.getPolicy(airline, "ECONOMY");
                }
                case "flight_status" -> data = flightStatusService.getStatus(parsed.getEntities().getFlightNumber());
                default -> { /* greeting / unknown -> no extra data needed */ }
            }
        }

        response.setData(data);
        return response;
    }
}
