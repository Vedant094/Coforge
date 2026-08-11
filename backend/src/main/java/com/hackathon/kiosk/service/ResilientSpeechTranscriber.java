package com.hackathon.kiosk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Single entry point for speech-to-text. Same "mock / live / auto" switch
 * as {@link ResilientNluService} - see that class for the full rationale.
 */
@Service
@Primary
public class ResilientSpeechTranscriber implements SpeechTranscriber {

    private static final Logger log = LoggerFactory.getLogger(ResilientSpeechTranscriber.class);

    private final SpeechTranscriber live;
    private final SpeechTranscriber mock;

    @Value("${kiosk.ai.mode:auto}")
    private String mode;

    public ResilientSpeechTranscriber(@Qualifier("live") SpeechTranscriber live,
                                       @Qualifier("mock") SpeechTranscriber mock) {
        this.live = live;
        this.mock = mock;
    }

    @Override
    public String transcribe(MultipartFile audioFile) {
        return switch (mode.toLowerCase()) {
            case "mock" -> mock.transcribe(audioFile);
            case "live" -> live.transcribe(audioFile);
            default -> tryLiveThenFallBack(audioFile);
        };
    }

    private String tryLiveThenFallBack(MultipartFile audioFile) {
        try {
            return live.transcribe(audioFile);
        } catch (Exception e) {
            log.warn("Whisper/Groq call failed ({}: {}) - falling back to the offline mock transcriber.",
                    e.getClass().getSimpleName(), e.getMessage());
            return mock.transcribe(audioFile);
        }
    }
}
