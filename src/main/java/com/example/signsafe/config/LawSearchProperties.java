package com.example.signsafe.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 법령 검색 API 호출 설정.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "law.search")
public class LawSearchProperties {
	// 검색 사용 여부
	private boolean enabled;
	// 검색 API 기본 URL (예: https://www.law.go.kr/DRF/lawSearch.do)
	private String baseUrl;
	// target 값 (예: law)
	private String target;
	// type 값 (예: XML)
	private String type;
	// 검색어 파라미터 이름 (예: query)
	private String keywordParam;
	// 페이지 파라미터 이름 (예: page)
	private String pageParam;
	// 페이지 크기 파라미터 이름 (예: display)
	private String pageSizeParam;
	// 페이지 크기
	private int pageSize;
	// 최대 페이지 수
	private int maxPages;
	// 검색 키워드 목록
	private List<String> keywords = new ArrayList<>();
}
