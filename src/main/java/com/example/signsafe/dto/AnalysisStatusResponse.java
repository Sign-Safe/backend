package com.example.signsafe.dto;

import com.example.signsafe.entity.AnalysisJobStatus;
import lombok.*;

import java.time.Instant;

/**
 * 분석 진행 상태를 프론트에 알려주기 위한 응답 DTO입니다.
 *
 * <p>사용처: {@code GET /api/analysis/{documentId}/status} (및 start 응답)</p>
 */
@Getter
@AllArgsConstructor
public class AnalysisStatusResponse {
	private Long documentId;
	private Long analysisJobId;
	private AnalysisJobStatus status;
	private Instant startedAt;
	private Instant finishedAt;
	private String errorMessage;
}
