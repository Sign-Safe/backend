package com.example.signsafe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 텍스트 입력으로 문서를 생성할 때 사용하는 요청 DTO입니다.
 *
 * <p>사용처: {@code POST /api/documents/text}</p>
 */
@Data
public class CreateDocumentFromTextRequest {
	/** 분석할 원문 텍스트(필수) */
	@NotBlank
	private String text;

	/** (옵션) 사용자 식별자. 멀티유저가 되면 토큰/세션에서 추출하는 방식으로 대체 가능 */
	private String userId;
}
