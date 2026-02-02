package com.example.signsafe.dto.lawgov;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 법령 기본정보 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LawBasicInfo {
	@JacksonXmlProperty(localName = "법령ID")
	private String lawId;

	@JacksonXmlProperty(localName = "법령명_한글")
	private String lawName;
}
