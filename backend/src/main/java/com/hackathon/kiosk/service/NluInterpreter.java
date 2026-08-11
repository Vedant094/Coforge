package com.hackathon.kiosk.service;

import com.hackathon.kiosk.dto.GeminiParsedResponse;

/**
 * Contract for anything that can turn a passenger's raw message into a
 * {@link GeminiParsedResponse} (detected language, intent, entities, reply).
 *
 * Two implementations exist:
 *  - {@link GeminiService}     - calls the real Gemini API over the network.
 *  - {@link MockNluService}    - rule-based, fully offline, never fails, never rate-limited.
 *
 * {@link ResilientNluService} is the bean everything else actually depends on;
 * it picks between the two based on config and network health, so the rest
 * of the app never needs to know which one answered.
 */
public interface NluInterpreter {
    GeminiParsedResponse interpret(String sessionId, String userMessage);
}
