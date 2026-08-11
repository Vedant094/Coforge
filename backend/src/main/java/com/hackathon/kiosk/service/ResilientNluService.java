package com.hackathon.kiosk.service;

import com.hackathon.kiosk.dto.GeminiParsedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Single entry point the rest of the app talks to for NLU. Controllers
 * depend on {@link NluInterpreter}, not on Gemini or the mock directly -
 * Spring wires this bean in because it is {@code @Primary}.
 *
 * Behaviour is controlled by {@code kiosk.ai.mode} in application.properties:
 *
 *  - "mock" - always use the offline rule-based engine. No network call is
 *             ever attempted. Unlimited, free, deterministic - good default
 *             for local dev, demos, and CI.
 *  - "live" - always call the real Gemini API. Fails loudly if it fails,
 *             same behaviour as the original hackathon code.
 *  - "auto" (default) - try Gemini first; if it throws for any reason
 *             (network error, timeout, invalid/expired key, quota
 *             exceeded, malformed response), log a warning and transparently
 *             fall back to the mock engine so the passenger-facing flow
 *             never breaks.
 */
@Service
@Primary
public class ResilientNluService implements NluInterpreter {

    private static final Logger log = LoggerFactory.getLogger(ResilientNluService.class);

    private final NluInterpreter live;
    private final NluInterpreter mock;

    @Value("${kiosk.ai.mode:auto}")
    private String mode;

    public ResilientNluService(@Qualifier("live") NluInterpreter live,
                                @Qualifier("mock") NluInterpreter mock) {
        this.live = live;
        this.mock = mock;
    }

    @Override
    public GeminiParsedResponse interpret(String sessionId, String userMessage) {
        return switch (mode.toLowerCase()) {
            case "mock" -> mock.interpret(sessionId, userMessage);
            case "live" -> live.interpret(sessionId, userMessage);
            default -> tryLiveThenFallBack(sessionId, userMessage);
        };
    }

    private GeminiParsedResponse tryLiveThenFallBack(String sessionId, String userMessage) {
        try {
            return live.interpret(sessionId, userMessage);
        } catch (Exception e) {
            log.warn("Gemini call failed ({}: {}) - falling back to the offline mock NLU engine.",
                    e.getClass().getSimpleName(), e.getMessage());
            return mock.interpret(sessionId, userMessage);
        }
    }
}
