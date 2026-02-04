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

    // 위험 조항 박스
    private static final String SYSTEM_INSTRUCTION = """
            당신은 계약서 독소조항을 분석하는 법률 전문가입니다.
            사용자가 입력한 계약서 본문을 분석해서, '을'에게 불리하거나 무효/분쟁 가능성이 있는 조항(독소조항)만 뽑아서 정리하세요.
            반드시 아래의 [출력 형식]를 참고하고 [규칙]을 엄격히 준수하여 작성해 주세요.

            매우 중요:
            - 이 응답은 '위험 조항' 박스에 그대로 표시됩니다.
            - 따라서 '종합 의견/전체 총평/권고' 같은 내용은 절대 쓰지 마세요.
            - 조항별로 아래 출력 형식을 반드시 지켜 작성하세요.

            출력 형식(마크다운, 조항마다 반복):
            {번호}. {조항명/주제}
            독소조항: [원문을 정확히 인용하되, 너무 길면 핵심 부분만 발췌]
            문제점: [해당 조항이 왜 문제인지, 어떤 불이익이 발생하는지 1-3문장으로 명확하고 정확하게 설명]
            법령 근거:[관련 법령명 및 조항] - [법령의 핵심 내용을 한 문장으로 설명]
            [빈줄]
            [이하 동일한 형식으로 반복]

            규칙:
            - 번호는 1부터 시작
            - 독소조항은 원문을 정확히 인용하되, 너무 길면 핵심 부분만 발췌
            - 법령 근거는 '민법 제103조'처럼 조문 형태로 구체적으로
            - 위험 조항이 없다면: "위험 조항이 없습니다." 한 줄만 출력
            - 문제점은 구체적인 불이익과 불공정성을 명확히 지적
            - 말투: 전문적이고 단호하며 신뢰감을 주는 법률 전문가의 어조를 유지할 것.
            """;

    private static final String NOT_A_CONTRACT_MESSAGE = "올바른 계약서 형태가 아닙니다. 계약서 본문을 입력해 주세요.";

    // 수정 제안 박스
    private static final String SUGGESTION_INSTRUCTION = """
            당신은 계약서의 독소조항을 수정하는 법률 전문가입니다.
            원본 계약서의 독소조항을 공정하고 합리적인 내용으로 수정하되, 원본의 구조와 형식을 최대한 유지하세요.

            ## 작성 규칙
            1. **원본 유지 원칙**
               - 독소조항이 아닌 모든 조항은 원본 그대로 복사
               - 조항 번호, 제목, 형식, 순서를 절대 변경하지 말 것
               - 띄어쓰기, 문단 구분, 들여쓰기 등 원본의 형식을 정확히 유지

            2. **독소조항 수정 원칙**
               - 독소조항으로 판별된 조항만 수정
               - 조항의 제목과 번호는 유지하고 본문 내용만 수정
               - 원문의 구조를 최대한 유지하되, 불공정한 부분을 공정하게 수정
               - 한쪽에만 유리한 조건을 양측에 균형있게 조정

            3. **수정 방향**
               - 무제한 → 합리적 범위 내로 제한
               - 일방적 권리 → 쌍방의 균형있는 권리와 의무
               - 과도한 책임 → 적정 수준의 책임
               - 불명확한 기준 → 명확하고 객관적인 기준
               - 부당한 제약 → 합리적인 제약

            4. **형식 유지**
               - 조항 번호는 절대 변경 금지 (예: 제1조, 제2조, ...)
               - 조항 제목은 절대 변경 금지 (예: (업무 범위), (위약금) ...)
               - 원본에 있던 특수문자, 따옴표, 괄호 등을 그대로 유지
               - 원본의 문단 구분과 줄바꿈을 정확히 따를 것

            5. **금지사항**
               - 조항을 추가하거나 삭제하지 말 것
               - 조항의 순서를 변경하지 말 것
               - 독소조항이 아닌 부분을 임의로 수정하지 말 것
               - 원본에 없던 내용을 새로 작성하지 말 것

            ## 주의사항
            - 원본의 모든 내용을 출력해야 합니다 (독소조항이 아닌 부분도 모두 포함)
            - 조항 번호와 제목은 절대 변경하지 마세요
            - 원본에 있는 조항을 임의로 삭제하거나 통합하지 마세요
            - 수정된 내용이 자연스럽고 법률적으로 타당해야 합니다
            - 양 당사자에게 공정하고 균형있는 내용이어야 합니다
            - 원본의 서식(번호 매기기, 들여쓰기, 따옴표 등)을 정확히 유지하세요
            """;

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
                        message,
                        null
                );
            }

            // 1) 요약 생성(원문 기반)
            String summary = generateSummary(userPrompt);

            // 2) 핵심 진단 결과 생성(위험 조항 나열 제외, 결론/권고 중심)
            String coreResult = generateCoreResult(userPrompt);

            // 3) 수정 제안 생성(원문 기반, 원본 형식 유지)
            String suggestion = generateSuggestion(userPrompt);

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
                    suggestion,
                    saved.getCreatedAt()
            );
        } catch (Exception e) {
            System.err.println("DB 저장 혹은 AI 분석 중 에러: " + e.getMessage());
            throw new IllegalStateException("분석 중 에러가 발생했습니다.", e);
        }
    }

    // 요약 박스
    private String generateSummary(String contractText) {
        List<Content> contents = new ArrayList<>();
        contents.add(Content.builder()
                .role("user")
                .parts(List.of(Part.fromText("""
                        당신은 계약서 독소조항을 분석하는 법률 전문가입니다. 아래 [계약서 본문]을 분석이나 평가는 절대 하지말고 사실관계 위주로 간단 명료하게 '요약'을 보고하십시오. 반드시 아래의 [출력 형식 및 구조]를 참고하고 [제약 사항]을 엄격히 준수하여 작성해 주세요.
                        
                        [출력 형식 및 구조]
                        한 줄 제목(계약 유형 추정)
                        [빈줄]
                        • 핵심 요약 3~10줄 (당사자/대상/대금/기간/해지/책임 중심)
                        
                        
                        [제약 사항]
                        1. 말투: 전문적이고 단호하며 신뢰감을 주는 법률 전문가의 어조를 유지할 것.
                        2. 금지: '제X조', '항', '호' 등 구체적인 조항 번호는 절대 언급하지 말 것.
                        3. [출력 형식 및 구조]와 [예시 출력]을 참고하고 [제약 사항]을 엄격히 준수할 것.
                        

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

    // 핵심 진단 결과 박스
    private String generateCoreResult(String contractText) {
        // 핵심 진단 결과는 '위험 조항 나열(조항별 상세)'을 제외하고,
        // 종합 의견/결론/권고사항만 짧게 보여주기 위한 별도 프롬프트로 생성
        List<Content> contents = new ArrayList<>();
        contents.add(Content.builder()
                .role("user")
                .parts(List.of(Part.fromText("""
                        당신은 계약서 독소조항을 분석하는 법률 전문가입니다. 아래 [계약서 본문]을 검토하여 '핵심 진단 결과'를 보고하십시오. 반드시 아래의 [출력 형식 및 구조]와 [예시 출력]을 참고하고 [제약 사항]을 엄격히 준수하여 작성해 주세요.
                        
                        [출력 형식 및 구조]
                        종합 위험도
                        {위험도를 "매우 높음/높음/중간/낮음" 중 하나로 명확히 판단하고, 한 문장으로 핵심 이유를 설명}
                        
                        주요 문제점
                        {심각한 독소조항을 구체적으로 지적. 각 문제점은 "~입니다", "~되어 있습니다" 등 단정적 어미로 종결. 조항 번호는 절대 언급하지 말고, "계약 해지 관련 조항", "손해배상 책임 조항" 등 내용으로만 지칭}
                        
                        핵심 리스크
                        {이 계약을 그대로 체결할 경우 발생 가능한 구체적인 법적/재정적 피해를 나열. "귀하는 ~할 수 있습니다", "~위험이 존재합니다" 등의 표현 사용}
                        
                        권고
                        {계약 진행 여부에 대한 명확한 권고("재검토 필수", "전문가 자문 후 진행", "수정 후 체결 가능" 등)와 다음 단계 행동을 간결하게 제시}
                        
                        [예시 출력]
                        종합 위험도
                        높음. 일방적으로 불리한 손해배상 및 계약해지 조건이 다수 포함되어 있습니다.
                        
                        주요 문제점
                        계약 해지 시 과도한 위약금이 부과되며, 귀책사유와 무관하게 모든 손해배상 책임을 일방 당사자가 전적으로 부담하도록 규정되어 있습니다. 또한 분쟁 발생 시 상대방에게만 유리한 관할 법원이 지정되어 있습니다.
                        
                        핵심 리스크
                        계약 해지 시 수천만 원 이상의 위약금이 청구될 수 있으며, 상대방의 귀책사유로 발생한 손해에 대해서도 귀하가 배상해야 할 위험이 존재합니다. 분쟁 발생 시 지리적으로 불리한 법원에서 소송을 진행해야 합니다.
                        
                        
                        권고
                        현 상태로 계약 체결 시 중대한 법적 불이익이 예상됩니다. 변호사 검토를 통한 조항 수정 후 재협상을 강력히 권고합니다.
                        
                        [제약 사항]
                        1. 말투: 전문적이고 단호하며 신뢰감을 주는 법률 전문가의 어조를 유지할 것.
                        2. 금지: '제X조', '항', '호' 등 구체적인 조항 번호는 절대 언급하지 말 것.
                        3. [출력 형식 및 구조]와 [예시 출력]을 참고하고 [제약 사항]을 엄격히 준수할 것.
                        4. 간격은 아래와 같이 무조건 빈 줄로 구분할 것.
                            종합 위험도
                            [설명]
                            [빈줄]
                            주요 문제점
                            [문제점 작성]
                            [빈줄]
                            핵심 리스크
                            [리스크 작성]
                            [빈줄]
                            [빈줄]
                             권고
                            [권고사항을 자연스러운 문단으로 작성]
                        
                
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

    // 수정 제안 박스
    private String generateSuggestion(String contractText) {
        List<Content> contents = new ArrayList<>();
        contents.add(Content.builder()
                .role("user")
                .parts(List.of(Part.fromText("""
                        [계약서 본문]
                        """ + contractText)))
                .build());

        GenerateContentConfig cfg = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(SUGGESTION_INSTRUCTION)))
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

