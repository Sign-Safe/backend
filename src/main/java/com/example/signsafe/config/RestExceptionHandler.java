package com.example.signsafe.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 프론트가 디버깅하기 쉽도록, JSON 파싱 실패(잘못된 본문 등)를
 * Spring Boot 기본 XML/Map 응답 대신 JSON으로 통일한다.
 */
@RestControllerAdvice
public class RestExceptionHandler {
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", 400);
		body.put("error", "Bad Request");
		body.put("message", "요청 본문을 읽을 수 없습니다. Content-Type과 JSON 형식을 확인해주세요.");
		body.put("detail", ex.getMostSpecificCause() == null ? ex.getMessage() : ex.getMostSpecificCause().getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body);
	}
}
