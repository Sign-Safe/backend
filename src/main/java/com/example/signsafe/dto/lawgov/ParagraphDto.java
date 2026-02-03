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
 * 항 DTO.
 * - paragraphContent는 조문 내용에 이어서 합쳐진다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParagraphDto {
	// 항 번호
	@JacksonXmlProperty(localName = "항번호")
	private String paragraphNo;

	// 항 내용
	@JacksonXmlProperty(localName = "항내용")
	private String paragraphContent;

	// 하위 호 목록
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "호")
	private List<SubItemDto> subItems = new ArrayList<>();
}
