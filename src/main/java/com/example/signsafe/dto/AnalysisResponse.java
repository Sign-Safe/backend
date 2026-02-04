package com.example.signsafe.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResponse(
        Long analysisId,
        String title,
        String userPrompt,
        String analysis,
        LocalDateTime createdAt,
        boolean lawDataUsed,
        List<String> lawKeywords,
        List<String> lawSnippets
) {
}
