package com.example.signsafe.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client client;

    @Value("${gemini.api.model}")
    private String modelName;

    private final List<Content> chatHistory = new ArrayList<>();

    public String getCompletion(String userPrompt) {
        try {
            // 사용자 질문을 추가
            chatHistory.add(Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(userPrompt)))
                    .build());

            // 모든 대화 내역을 제미나이한테 전달하기
            GenerateContentResponse response = client.models.generateContent(modelName, chatHistory, null);

            // 제마나이 답 추출하기
            String botResponse = response.text();

            // 제미나이 답변을 대화 내역에 추가
            chatHistory.add(Content.builder()
                    .role("model")
                    .parts(List.of(Part.fromText(botResponse)))
                    .build());

            return botResponse;
        } catch (Exception e) {
            return "오류가 발생했습니다: " + e.getMessage();
        }
    }

    // 대화 내역 초기화 메서드
    public void clearHistory() {
        chatHistory.clear();
    }
}