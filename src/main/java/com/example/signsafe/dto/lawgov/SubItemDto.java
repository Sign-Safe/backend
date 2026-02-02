package com.example.signsafe.dto.lawgov;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 호 DTO.
 * - 항의 하위 항목(호) 내용을 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubItemDto {
	// 호 번호
	@JacksonXmlProperty(localName = "호번호")
	private String subItemNo;

	// 호 내용
	@JacksonXmlProperty(localName = "호내용")
	private String subItemContent;
}
