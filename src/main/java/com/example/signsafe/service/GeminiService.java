package com.example.signsafe.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client client;

    @Value("${gemini.api.model}")
    private String modelName;

    public String getCompletion(String prompt) {
        try {
            GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);
            return response.text();
        } catch (Exception e) {
            return "오류가 발생했습니다: " + e.getMessage();
        }
    }
}