package com.example.signsafe.dto.lawgov;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 법령 XML 응답의 최상위 DTO.
 * - basicInfo: 법령 기본 정보(법령ID/법령명 등)
 * - articles: 조문 단위 목록
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LawEflawResponse {
	@JacksonXmlProperty(localName = "기본정보")
	private LawBasicInfo basicInfo;

	@JacksonXmlElementWrapper(localName = "조문")
	@JacksonXmlProperty(localName = "조문단위")
	private List<ArticleDto> articles = new ArrayList<>();
}
