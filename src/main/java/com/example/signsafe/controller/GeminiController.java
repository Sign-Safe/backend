package com.example.signsafe.controller;

import com.example.signsafe.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class GeminiController {

    private final GeminiService geminiService;

    public record AnalysisRequest(String prompt, String uuid) {}

    @PostMapping("/ask")
    public String ask(@RequestBody AnalysisRequest request) {
        return geminiService.getCompletion(request.prompt(), request.uuid());
    }
}