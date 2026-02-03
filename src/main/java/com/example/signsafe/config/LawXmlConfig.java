package com.example.signsafe.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LawXmlConfig {
	@Bean
	public XmlMapper xmlMapper() {
		// 법제처 XML 응답은 필드가 변동될 수 있어 알 수 없는 필드는 무시
		XmlMapper mapper = new XmlMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		return mapper;
	}
}
