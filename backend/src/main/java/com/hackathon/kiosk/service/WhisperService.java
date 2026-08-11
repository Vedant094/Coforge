package com.hackathon.kiosk.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Live speech-to-text provider - sends recorded audio to Groq's free
 * Whisper endpoint (OpenAI-compatible). Whisper auto-detects the spoken
 * language from the audio itself - no language hint needed from the
 * frontend. Wrapped by {@link ResilientSpeechTranscriber}.
 */
@Service
@Qualifier("live")
public class WhisperService implements SpeechTranscriber {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${kiosk.ai.live.timeout-ms:4000}")
    private long timeoutMs;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    @Override
    @SuppressWarnings("unchecked")
    public String transcribe(MultipartFile audioFile) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", audioFile.getResource())
                    .filename(audioFile.getOriginalFilename() != null
                            ? audioFile.getOriginalFilename() : "recording.webm");
            builder.part("model", "whisper-large-v3-turbo");
            builder.part("response_format", "json");

            Map<String, Object> response = webClient.post()
                    .uri("/audio/transcriptions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response == null || response.get("text") == null) {
                return "";
            }
            return response.get("text").toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Whisper transcription failed: " + e.getMessage(), e);
        }
    }
}
