package com.hackathon.kiosk.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Contract for anything that can turn recorded audio into text.
 *
 *  - {@link WhisperService}       - calls Groq's hosted Whisper API.
 *  - {@link MockWhisperService}   - fully offline, returns a deterministic
 *    canned transcript picked from the audio's own byte signature, so the
 *    same recording always "transcribes" the same way during a demo.
 *
 * {@link ResilientSpeechTranscriber} is the bean everything else depends on.
 */
public interface SpeechTranscriber {
    String transcribe(MultipartFile audioFile);
}
