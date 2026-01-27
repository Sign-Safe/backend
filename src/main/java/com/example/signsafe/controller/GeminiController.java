package com.example.signsafe.controller;

import com.example.signsafe.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @GetMapping("/ask")
    public String ask(
            @RequestParam String prompt,
            @RequestParam String uuid  // 크롬 주소창에서 &uuid=값 형태로 일단 임시...
    ) {
        return geminiService.getCompletion(prompt, uuid);
    }
}