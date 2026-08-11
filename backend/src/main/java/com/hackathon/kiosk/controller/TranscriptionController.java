package com.hackathon.kiosk.controller;

import com.hackathon.kiosk.service.SpeechTranscriber;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/transcribe")
public class TranscriptionController {

    private final SpeechTranscriber whisperService;

    // Spring injects the @Primary SpeechTranscriber bean here (ResilientSpeechTranscriber).
    public TranscriptionController(SpeechTranscriber whisperService) {
        this.whisperService = whisperService;
    }

    @PostMapping
    public Map<String, String> transcribe(@RequestParam("audio") MultipartFile audio) {
        String text = whisperService.transcribe(audio);
        return Map.of("text", text);
    }
}
