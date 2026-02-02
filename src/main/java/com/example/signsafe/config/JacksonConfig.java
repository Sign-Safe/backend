package com.example.signsafe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 기본 JSON(ObjectMapper)과 XML(XmlMapper) 빈을 명확히 분리한다.
 *
 * - XmlMapper는 LawXmlConfig에서 별도 Bean으로 제공됨
 * - JSON 응답/요청 바인딩은 이 ObjectMapper(Primary)가 우선 사용되도록 한다.
 */
@Configuration
public class JacksonConfig {
	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}
}
