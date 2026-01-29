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
     * 제미나이한테 분석을 요청하고 질문과 결과를 모두 DB에 저장
     */
    public String getCompletion(String userPrompt, String guestUuid) {
        return analyzeText(userPrompt, guestUuid, "계약서 분석 요청").analysis();
    }

    public AnalysisResponse analyzeText(String userPrompt, String guestUuid, String title) {
        try {
            // 제미나이한테 보낼 대화 리스트 생성
            List<Content> chatHistory = new ArrayList<>();
            chatHistory.add(Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(userPrompt)))
                    .build());

            // AI 답변 생성
            GenerateContentResponse response = client.models.generateContent(modelName, chatHistory, null);
            String botResponse = response.text();

            // DB 저장
            ContractAnalysis analysis = ContractAnalysis.builder()
                    .guestUuid(guestUuid)
                    .contractTitle(title == null || title.isBlank() ? "계약서 분석 요청" : title)
                    .userPrompt(userPrompt)      // 사용자의 질문 저장
                    .analysisResult(botResponse) // AI의 분석 답변 저장
                    .build();

            ContractAnalysis saved = repository.save(analysis);

            // 프론트엔드에 돌려줄 응답 객체 생성
            return new AnalysisResponse(
                    saved.getId(),
                    saved.getContractTitle(),
                    saved.getUserPrompt(),
                    saved.getAnalysisResult(),
                    saved.getCreatedAt()
            );
        } catch (Exception e) {
            System.err.println("DB 저장 혹은 AI 분석 중 에러: " + e.getMessage());
            throw new IllegalStateException("분석 중 에러가 발생했습니다.", e);
        }
    }
}