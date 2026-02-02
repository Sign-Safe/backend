package com.example.signsafe.dto.lawgov;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 조문 DTO.
 * - articleContent + 하위 항/호 내용을 합쳐 AI 검색용 content를 생성한다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleDto {
	// 조문 번호
	@JacksonXmlProperty(localName = "조문번호")
	private String articleNo;

	// 조문 제목
	@JacksonXmlProperty(localName = "조문제목")
	private String articleTitle;

	// 조문 본문 내용
	@JacksonXmlProperty(localName = "조문내용")
	private String articleContent;

	// 하위 항 목록
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "항")
	private List<ParagraphDto> paragraphs = new ArrayList<>();
}
