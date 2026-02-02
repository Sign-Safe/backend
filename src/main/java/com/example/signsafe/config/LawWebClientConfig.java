package com.example.signsafe.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class LawWebClientConfig {
	@Bean
	public WebClient lawWebClient(LawApiProperties properties) {
		// 법제처 API 호출용 WebClient에 응답 타임아웃을 설정
		HttpClient httpClient = HttpClient.create()
			.responseTimeout(Duration.ofMillis(properties.getTimeoutMs()));

		var strategies = org.springframework.web.reactive.function.client.ExchangeStrategies.builder()
			.codecs(configurer -> configurer.defaultCodecs()
				.maxInMemorySize(properties.getMaxInMemorySize()))
			.build();

		// baseUrl을 고정해 매 요청 시 경로/쿼리만 지정하도록 구성
		return WebClient.builder()
			.baseUrl(properties.getBaseUrl())
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.exchangeStrategies(strategies)
			.build();
	}
}
