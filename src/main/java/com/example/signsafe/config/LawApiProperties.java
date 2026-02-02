package com.example.signsafe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 법제처 API 호출에 필요한 설정값을 바인딩하는 설정 클래스.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "law.api")
public class LawApiProperties {
	// API 기본 URL (예: http://www.law.go.kr/DRF/lawService.do)
	private String baseUrl;
	// OC 값 (법제처에서 발급된 사용자 식별값)
	private String oc;
	// target 값 (예: eflaw)
	private String target;
	// type 값 (예: XML)
	private String type;
	// WebClient 응답 타임아웃(ms)
	private int timeoutMs;
	// WebClient 최대 응답 버퍼 크기(byte)
	private int maxInMemorySize;
	// 애플리케이션 시작 시 자동 수집 실행 여부
	private boolean runOnStartup;
	// 시작 시 샘플로 수집할 법령 ID (law.search.enabled=true 또는 lawIds가 있으면 사용되지 않음)
	private String sampleLawId;
	// 시작 시 수집할 법령 ID 목록(콤마 구분)
	private java.util.List<String> lawIds = new java.util.ArrayList<>();
}
