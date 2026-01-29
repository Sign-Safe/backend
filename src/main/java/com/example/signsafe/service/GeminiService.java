package com.example.signsafe.service;

import com.example.signsafe.dto.AnalysisResponse;
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

    /**
     * 제미나이한테 분석을 요청하고 결과를 DB에 저장
     * @param userPrompt 사용자가 입력한 질문(계약서 내용)
     * @param guestUuid  비회원 식별을 위한 고유 ID
     */
    public String getCompletion(String userPrompt, String guestUuid) {
        return analyzeText(userPrompt, guestUuid, "계약서 분석 요청").analysis();
    }

    public AnalysisResponse analyzeText(String userPrompt, String guestUuid, String title) {
        try {
            List<Content> chatHistory = new ArrayList<>();
            chatHistory.add(Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(userPrompt)))
                    .build());

            GenerateContentResponse response = client.models.generateContent(modelName, chatHistory, null);
            String botResponse = response.text();

            ContractAnalysis analysis = ContractAnalysis.builder()
                    .guestUuid(guestUuid)
                    .contractTitle(title == null || title.isBlank() ? "계약서 분석 요청" : title)
                    .analysisResult(botResponse)
                    .build();

            ContractAnalysis saved = repository.save(analysis);
            return new AnalysisResponse(
                    saved.getId(),
                    saved.getContractTitle(),
                    saved.getAnalysisResult(),
                    saved.getCreatedAt()
            );
        } catch (Exception e) {
            throw new IllegalStateException("분석 중 에러가 발생했습니다.", e);
        }
    }
}