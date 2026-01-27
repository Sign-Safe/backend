package com.example.signsafe.service;

import com.example.signsafe.entity.ContractAnalysis;
import com.example.signsafe.repository.ContractAnalysisRepository;
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
    private final ContractAnalysisRepository repository;

    @Value("${gemini.api.model}")
    private String modelName;

    private final List<Content> chatHistory = new ArrayList<>();

    /**
     * 제미나이한테 분석을 요청하고 결과를 DB에 저장
     * @param userPrompt 사용자가 입력한 질문(계약서 내용)
     * @param guestUuid  비회원 식별을 위한 고유 ID
     */
    public String getCompletion(String userPrompt, String guestUuid) {
        try {
            // 1. 사용자의 질문을 대화 기록에 추가
            chatHistory.add(Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(userPrompt)))
                    .build());

            // 2. 제미나이 모델 호출
            GenerateContentResponse response = client.models.generateContent(modelName, chatHistory, null);
            String botResponse = response.text();

            // 3. AI의 답변을 대화 기록에 추가
            chatHistory.add(Content.builder()
                    .role("model")
                    .parts(List.of(Part.fromText(botResponse)))
                    .build());

            // 4. DB에 분석 결과 저장
            ContractAnalysis analysis = ContractAnalysis.builder()
                    .guestUuid(guestUuid)
                    .contractTitle("계약서 분석 요청")
                    .analysisResult(botResponse)
                    .build();

            repository.save(analysis);

            return botResponse;

        } catch (Exception e) {
            System.err.println("분석 중 에러 발생: " + e.getMessage());
            return "오류가 발생했습니다: " + e.getMessage();
        }
    }
}