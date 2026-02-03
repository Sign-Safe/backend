package com.example.signsafe.service;

import com.example.signsafe.dto.AnalysisResponse;
import com.example.signsafe.entity.ContractAnalysis;
import com.example.signsafe.repository.ContractAnalysisRepository;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import com.google.genai.types.ThinkingConfig;
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

    private static final String SYSTEM_INSTRUCTION = """
            너는 20년 차 법률 전문가이자 계약서 독소조항(위험 조항) 탐지기야.
            사용자가 입력한 계약서 본문을 분석해서, '을'에게 불리하거나 무효/분쟁 가능성이 있는 조항(독소조항)만 뽑아 정리해.

            매우 중요:
            - 이 응답은 '위험 조항' 박스에 그대로 표시된다.
            - 따라서 '종합 의견/전체 총평/권고' 같은 내용은 절대 쓰지 마. (그건 별도 coreResult에서 처리한다)
            - 조항별로 아래 출력 형식을 반드시 지켜.
            - 계약서와 무관한 입력이면 반드시 "올바른 계약서 형태가 아니다" 라고만 답해.

            출력 형식(마크다운, 조항마다 반복):
            ### {번호}. {조항명/주제}
            * **독소조항:** "..."
            * **문제점:** ...
            * **법령 근거:** ...

            규칙:
            - 번호는 1부터 시작
            - 독소조항은 계약서 원문 표현을 최대한 유지해서 인용 형태로 작성
            - 법령 근거는 가능하면 '민법 제103조'처럼 조문 형태로 구체적으로
            - 위험 조항이 없다면: "위험 조항이 없습니다." 한 줄만 출력
            """;

    private static final String NOT_A_CONTRACT_MESSAGE = "올바른 계약서 형태가 아닙니다. 계약서 본문을 입력해 주세요.";

    /**
     * 제미나이한테 분석을 요청하고 질문과 결과를 모두 DB에 저장
     */
    public String getCompletion(String userPrompt, String guestUuid) {
        return analyzeText(userPrompt, guestUuid, "계약서 분석 요청").analysis();
    }

    public AnalysisResponse analyzeText(String userPrompt, String guestUuid, String title) {
        try {
            if (!looksLikeContractText(userPrompt)) {
                // 계약서가 아닌 입력은 AI 호출/DB 저장 없이 즉시 응답
                String message = NOT_A_CONTRACT_MESSAGE;
                return new AnalysisResponse(
                        null,
                        title == null || title.isBlank() ? "계약서 분석 요청" : title,
                        userPrompt,
                        message,
                        message,
                        message,
                        null
                );
            }

            // 1) 요약 생성(원문 기반)
            String summary = generateSummary(userPrompt);

            // 2) 핵심 진단 결과 생성(위험 조항 나열 제외, 결론/권고 중심)
            String coreResult = generateCoreResult(userPrompt);

            // 제미나이한테 보낼 대화 리스트 생성
            List<Content> chatHistory = new ArrayList<>();
            chatHistory.add(Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(userPrompt)))
                    .build());

            // Tools (Google Search) + System instruction + Thinking config
            List<Tool> tools = new ArrayList<>();
            tools.add(
                    Tool.builder()
                            .googleSearch(GoogleSearch.builder().build())
                            .build()
            );

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .thinkingConfig(ThinkingConfig.builder().build())
                    .tools(tools)
                    .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION)))
                    .build();

            // AI 답변 생성(위험 조항 상세 포함)
            GenerateContentResponse response = client.models.generateContent(modelName, chatHistory, config);
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
                    summary,
                    coreResult,
                    saved.getCreatedAt()
            );
        } catch (Exception e) {
            System.err.println("DB 저장 혹은 AI 분석 중 에러: " + e.getMessage());
            throw new IllegalStateException("분석 중 에러가 발생했습니다.", e);
        }
    }

    private String generateSummary(String contractText) {
        // 요약은 계약서 '원문'을 기반으로 해야 해서 별도 프롬프트로 생성
        List<Content> contents = new ArrayList<>();
        contents.add(Content.builder()
                .role("user")
                .parts(List.of(Part.fromText("""
                        아래 계약서 본문을 분석/평가하지 말고, 사실관계 위주로 간단명료하게 요약해줘.
                        출력 형식:
                        - 한 줄 제목(계약 유형 추정)
                        - 핵심 요약 5줄 이내(당사자/대상/대금/기간/해지/책임 중심)

                        [계약서 본문]
                        """ + contractText)))
                .build());

        GenerateContentConfig cfg = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText("너는 계약서 본문을 요약하는 도우미야. 반드시 한국어로, 간결하게 적어.")))
                .build();

        GenerateContentResponse res = client.models.generateContent(modelName, contents, cfg);
        String text = res.text();
        return (text == null || text.isBlank()) ? null : text.trim();
    }

    private String generateCoreResult(String contractText) {
        // 핵심 진단 결과는 '위험 조항 나열(조항별 상세)'을 제외하고,
        // 종합 의견/결론/권고사항만 짧게 보여주기 위한 별도 프롬프트로 생성
        List<Content> contents = new ArrayList<>();
        contents.add(Content.builder()
                .role("user")
                .parts(List.of(Part.fromText("""
                        아래 계약서 본문을 20년 차 법률 전문가 관점에서 검토하되,
                        '위험 조항'을 조항별로 나열하거나 길게 설명하지 말고,
                        위험 조항을 제외한 핵심 결론(종합 의견)과 권고사항만 간결하게 작성해줘.

                        constraints:
                        - 한국어
                        - 8~12줄
                        - 조항 번호/제X조/항/호 같은 표기는 쓰지 말 것
                        - 법적 효력/무효 가능성/리스크 수준을 한 번은 명확히 언급
                        - 마지막 줄에는 '권고: ...' 형태로 요약 권고를 1줄로 끝낼 것

                        [계약서 본문]
                        """ + contractText)))
                .build());

        GenerateContentConfig cfg = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText("너는 계약서의 핵심 진단 결과(종합 의견)를 작성하는 도우미야. 반드시 한국어로, 간결하게 적어.")))
                .build();

        GenerateContentResponse res = client.models.generateContent(modelName, contents, cfg);
        String text = res.text();
        return (text == null || text.isBlank()) ? null : text.trim();
    }

    private boolean looksLikeContractText(String text) {
        if (text == null) return false;
        String t = text.trim();
        if (t.isEmpty()) return false;

        // 너무 짧으면(인사/한두 문장) 계약서로 보기 어려움
        if (t.length() < 80) return false;

        String lower = t.toLowerCase();

        String[] keywords = new String[] {
                "제1조", "제2조", "제3조", "제4조", "제5조",
                "조", "항", "호",
                "갑", "을",
                "계약", "계약서", "약관", "이용약관", "동의",
                "당사자", "체결",
                "대금", "지급", "납부", "청구",
                "기간", "갱신", "해지", "해제", "종료",
                "위약금", "손해배상", "배상", "책임", "면책",
                "비밀유지", "개인정보", "저작권",
                "분쟁", "관할", "준거법"
        };

        int hits = 0;
        for (String k : keywords) {
            if (t.contains(k) || lower.contains(k)) {
                hits++;
                if (hits >= 2) return true;
            }
        }

        if (t.matches("(?s).*(제\\s*\\d+\\s*조|\\d+\\.\\s*|\u2022|\u00B7|\\-\\s).*")) {
            return true;
        }

        return false;
    }
}