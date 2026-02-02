package com.example.signsafe.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.signsafe.config.LawApiProperties;
import com.example.signsafe.config.LawSearchProperties;
import com.example.signsafe.service.LawIngestionService;
import com.example.signsafe.service.LawSearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 애플리케이션 시작 시 샘플 법령을 수집하는 러너.
 */
@Component
public class LawIngestionRunner implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(LawIngestionRunner.class);
	private final LawApiProperties properties;
	private final LawSearchProperties lawSearchProperties;
	private final LawIngestionService lawIngestionService;
	private final LawSearchService lawSearchService;

	public LawIngestionRunner(
			LawApiProperties properties,
			LawSearchProperties lawSearchProperties,
			LawIngestionService lawIngestionService,
			LawSearchService lawSearchService) {
		this.properties = properties;
		this.lawSearchProperties = lawSearchProperties;
		this.lawIngestionService = lawIngestionService;
		this.lawSearchService = lawSearchService;
	}

	@Override
	public void run(String... args) {
		// 설정이 꺼져 있으면 실행하지 않음
		if (!properties.isRunOnStartup()) {
			return;
		}
		if (lawSearchProperties.isEnabled()) {
			var ids = lawSearchService.fetchLawIdsByKeywords(lawSearchProperties.getKeywords());
			if (ids.isEmpty()) {
				log.warn("Law search returned no IDs; skip fallback to sample law ID");
				return;
			}
			lawIngestionService.fetchAndSaveByLawIds(ids.stream().toList());
			return;
		}
		if (properties.getLawIds() != null && !properties.getLawIds().isEmpty()) {
			lawIngestionService.fetchAndSaveByLawIds(properties.getLawIds());
			return;
		}
		// 샘플 법령 ID가 없으면 실행하지 않음
		if (!StringUtils.hasText(properties.getSampleLawId())) {
			return;
		}
		lawIngestionService.fetchAndSaveByLawId(properties.getSampleLawId());
	}
}
