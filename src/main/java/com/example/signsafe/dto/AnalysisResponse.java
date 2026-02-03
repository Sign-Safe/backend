package com.example.signsafe.dto;

import java.time.LocalDateTime;

public record AnalysisResponse(
        Long analysisId,
        String title,
        String userPrompt,
        String analysis,
        String summary,
        LocalDateTime createdAt
) {
}
