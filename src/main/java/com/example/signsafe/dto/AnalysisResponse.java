package com.example.signsafe.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResponse(
        Long analysisId,
        String title,
        String userPrompt,
        String analysis,
        String summary,
        String coreResult,
        String suggestion,
        LocalDateTime createdAt,
        boolean lawDataUsed,
        List<String> lawKeywords,
        List<String> lawSnippets,
        boolean isContract
) {
}
