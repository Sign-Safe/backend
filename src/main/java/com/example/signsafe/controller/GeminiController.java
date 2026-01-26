package com.example.signsafe.controller;

import com.example.signsafe.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @GetMapping("/api/gemini/ask")
    public String ask(@RequestParam String prompt) {
        return geminiService.getCompletion(prompt);
    }
}