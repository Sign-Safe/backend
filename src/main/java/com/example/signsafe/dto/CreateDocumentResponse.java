package com.example.signsafe.dto;

import lombok.*;

/**
 * 문서 생성 API의 응답 DTO입니다.
 *
 * <p>documentId: 저장된 Document의 ID</p>
 * <p>analysisJobId: 즉시 생성된 분석 job ID</p>
 */
@Getter
@AllArgsConstructor
public class CreateDocumentResponse {
	private Long documentId;
	private Long analysisJobId;
}
