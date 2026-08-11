package com.hackathon.kiosk.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Offline stand-in for {@link WhisperService}. Real speech-to-text needs a
 * model, so this cannot transcribe the actual audio content - instead it
 * deterministically maps the recording to one of a bank of realistic kiosk
 * phrases based on the audio's own byte size. Same recording -> same
 * "transcript" every time, which is exactly what you want for demos and
 * repeatable tests, without touching the network or an API key.
 */
@Service
@Qualifier("mock")
public class MockWhisperService implements SpeechTranscriber {

    private static final List<String> SAMPLE_PHRASES = List.of(
        "Hi, I'd like to check in for my flight.",
        "Can I get a window seat please?",
        "How many bags am I allowed to check?",
        "What's the status of flight AI202?",
        "Where is gate 14?",
        "Namaste, mujhe check-in karna hai.",
        "Hola, quiero saber el estado de mi vuelo.",
        "I have my booking reference, can you check me in?"
    );

    @Override
    public String transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            return "";
        }
        long size;
        try {
            size = audioFile.getSize() > 0 ? audioFile.getSize() : audioFile.getBytes().length;
        } catch (IOException e) {
            size = audioFile.getSize();
        }
        int index = (int) (Math.abs(size) % SAMPLE_PHRASES.size());
        return SAMPLE_PHRASES.get(index);
    }
}
