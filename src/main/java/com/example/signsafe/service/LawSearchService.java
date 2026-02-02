package com.example.signsafe.service;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.example.signsafe.config.LawApiProperties;
import com.example.signsafe.config.LawSearchProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 법제처 검색 API로 법령 ID 목록을 수집한다.
 */
@Service
public class LawSearchService {
	private static final Logger log = LoggerFactory.getLogger(LawSearchService.class);
	private final WebClient lawWebClient;
	private final XmlMapper xmlMapper;
	private final LawApiProperties apiProperties;
	private final LawSearchProperties searchProperties;

	public LawSearchService(
			WebClient lawWebClient,
			XmlMapper xmlMapper,
			LawApiProperties apiProperties,
			LawSearchProperties searchProperties) {
		this.lawWebClient = lawWebClient;
		this.xmlMapper = xmlMapper;
		this.apiProperties = apiProperties;
		this.searchProperties = searchProperties;
	}

	public Set<String> fetchLawIdsByKeywords(List<String> keywords) {
		Set<String> results = new LinkedHashSet<>();
		if (keywords == null || keywords.isEmpty()) {
			return results;
		}
		for (String keyword : keywords) {
			if (!StringUtils.hasText(keyword)) {
				continue;
			}
			Set<String> ids = fetchLawIdsByKeyword(keyword.trim());
			log.info("Law search keyword='{}' ids={}", keyword, ids.size());
			results.addAll(ids);
		}
		log.info("Law search total unique ids={}", results.size());
		return results;
	}

	private Set<String> fetchLawIdsByKeyword(String keyword) {
		if (!StringUtils.hasText(apiProperties.getOc())) {
			throw new IllegalStateException("law.api.oc is required");
		}
		if (!StringUtils.hasText(searchProperties.getBaseUrl())) {
			throw new IllegalStateException("law.search.base-url is required");
		}

		Set<String> ids = new LinkedHashSet<>();
		for (int page = 1; page <= searchProperties.getMaxPages(); page++) {
			var requestUri = UriComponentsBuilder.fromHttpUrl(searchProperties.getBaseUrl())
				.queryParam("OC", apiProperties.getOc())
				.queryParam("target", searchProperties.getTarget())
				.queryParam("type", searchProperties.getType())
				.queryParam(searchProperties.getKeywordParam(), keyword)
				.queryParam(searchProperties.getPageParam(), page)
				.queryParam(searchProperties.getPageSizeParam(), searchProperties.getPageSize())
				.build()
				.encode(java.nio.charset.StandardCharsets.UTF_8)
				.toUri();

			log.info("Law search request: {}", requestUri);

			String xml = lawWebClient.get()
				.uri(requestUri)
				.retrieve()
				.bodyToMono(String.class)
				.block(Duration.ofMillis(apiProperties.getTimeoutMs()));

			if (!StringUtils.hasText(xml)) {
				break;
			}

			Set<String> pageIds = extractLawIds(xml);
			if (pageIds.isEmpty()) {
				break;
			}
			ids.addAll(pageIds);
		}

		return ids;
	}

	private Set<String> extractLawIds(String xml) {
		try {
			JsonNode root = xmlMapper.readTree(xml);
			Set<String> ids = new LinkedHashSet<>();
			collectIds(root, ids);
			return ids;
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to parse law search XML", ex);
		}
	}

	private void collectIds(JsonNode node, Set<String> ids) {
		if (node == null) {
			return;
		}
		if (node.isObject()) {
			node.fields().forEachRemaining(entry -> {
				String fieldName = entry.getKey();
				JsonNode value = entry.getValue();
				if ("법령ID".equals(fieldName) && value.isValueNode()) {
					String text = value.asText();
					if (StringUtils.hasText(text)) {
						ids.add(text.trim());
					}
				}
				collectIds(value, ids);
			});
		} else if (node.isArray()) {
			for (JsonNode item : node) {
				collectIds(item, ids);
			}
		}
	}
}
