package com.example.signsafe.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.example.signsafe.config.LawApiProperties;
import com.example.signsafe.dto.lawgov.ArticleDto;
import com.example.signsafe.dto.lawgov.LawBasicInfo;
import com.example.signsafe.dto.lawgov.LawEflawResponse;
import com.example.signsafe.entity.LawData;
import com.example.signsafe.repository.LawDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 법제처 XML을 수집하고 조문 단위로 변환해 DB에 저장하는 서비스.
 */
@Service
public class LawIngestionService {
	private static final Logger log = LoggerFactory.getLogger(LawIngestionService.class);
	private final WebClient lawWebClient;
	private final XmlMapper xmlMapper;
	private final LawApiProperties properties;
	private final LawDataRepository lawDataRepository;

	public LawIngestionService(
			WebClient lawWebClient,
			XmlMapper xmlMapper,
			LawApiProperties properties,
			LawDataRepository lawDataRepository) {
		this.lawWebClient = lawWebClient;
		this.xmlMapper = xmlMapper;
		this.properties = properties;
		this.lawDataRepository = lawDataRepository;
	}

	public List<LawData> fetchAndSaveByLawId(String lawId) {
		// 필수 파라미터 검증
		if (!StringUtils.hasText(properties.getOc())) {
			throw new IllegalStateException("law.api.oc is required");
		}
		if (!StringUtils.hasText(lawId)) {
			throw new IllegalArgumentException("lawId is required");
		}
		if (!StringUtils.hasText(properties.getBaseUrl())) {
			throw new IllegalStateException("law.api.base-url is required");
		}

		// 법제처 API 호출(현행법령 본문 조회)
		var requestUri = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
			.queryParam("OC", properties.getOc())
			.queryParam("target", properties.getTarget())
			.queryParam("type", properties.getType())
			.queryParam("ID", lawId)
			.build(true)
			.toUri();

		log.info("Law API request: {}", requestUri);

		String xml = lawWebClient.get()
			.uri(requestUri)
			.retrieve()
			.bodyToMono(String.class)
			.block(Duration.ofMillis(properties.getTimeoutMs()));

		if (!StringUtils.hasText(xml)) {
			log.warn("Law API response is empty");
			return List.of();
		}

		log.info("Law API response size: {} bytes", xml.length());

		// XML을 DTO로 파싱 후 엔티티로 변환
		LawEflawResponse response = parseLawResponse(xml);
		int articleCount = response == null || response.getArticles() == null ? 0 : response.getArticles().size();
		log.info("Parsed articles count: {}", articleCount);

		List<LawData> entities = toEntities(response);
		if (entities.isEmpty()) {
			log.warn("No entities to save (content empty or no articles)");
			return List.of();
		}

		log.info("Saving {} entities", entities.size());
		return lawDataRepository.saveAll(entities);
	}

	public List<LawData> fetchAndSaveByLawIds(List<String> lawIds) {
		List<LawData> results = new ArrayList<>();
		if (lawIds == null || lawIds.isEmpty()) {
			return results;
		}
		for (String lawId : lawIds) {
			if (!StringUtils.hasText(lawId)) {
				continue;
			}
			results.addAll(fetchAndSaveByLawId(lawId.trim()));
		}
		return results;
	}

	private LawEflawResponse parseLawResponse(String xml) {
		try {
			JsonNode root = xmlMapper.readTree(xml);
			log.info("XML root fields: {}", toFieldList(root));

			LawEflawResponse response = new LawEflawResponse();
			if (root.has("기본정보")) {
				response.setBasicInfo(xmlMapper.treeToValue(root.get("기본정보"), LawBasicInfo.class));
			}

			List<ArticleDto> articles = new ArrayList<>();
			if (root.has("조문")) {
				JsonNode articlesNode = root.get("조문");
				JsonNode itemsNode = articlesNode.has("조문단위") ? articlesNode.get("조문단위") : articlesNode;
				if (itemsNode.isArray()) {
					for (JsonNode item : itemsNode) {
						articles.add(xmlMapper.treeToValue(item, ArticleDto.class));
					}
				} else if (itemsNode.isObject()) {
					articles.add(xmlMapper.treeToValue(itemsNode, ArticleDto.class));
				}

				JsonNode firstArticle = itemsNode;
				if (firstArticle.isArray() && firstArticle.size() > 0) {
					firstArticle = firstArticle.get(0);
				}
				if (firstArticle.isObject()) {
					log.info("First article field names: {}", toFieldList(firstArticle));
				}
			}

			response.setArticles(articles);
			return response;
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to parse law XML", ex);
		}
	}

	private List<String> toFieldList(JsonNode node) {
		List<String> fields = new ArrayList<>();
		node.fieldNames().forEachRemaining(fields::add);
		return fields;
	}

	private List<LawData> toEntities(LawEflawResponse response) {
		List<LawData> entities = new ArrayList<>();
		if (response == null || response.getArticles() == null) {
			return entities;
		}

		String lawId = response.getBasicInfo() == null ? null : response.getBasicInfo().getLawId();
		String lawName = response.getBasicInfo() == null ? null : response.getBasicInfo().getLawName();

		int index = 0;
		int skippedDuplicates = 0;
		for (ArticleDto article : response.getArticles()) {
			if (index == 0) {
				int contentLength = article.getArticleContent() == null ? 0 : article.getArticleContent().length();
				int paragraphCount = article.getParagraphs() == null ? 0 : article.getParagraphs().size();
				log.info("First article fields: no={}, title={}, contentLen={}, paragraphs={}",
					article.getArticleNo(), article.getArticleTitle(), contentLength, paragraphCount);
			}
			index++;

			if (!StringUtils.hasText(lawId) || !StringUtils.hasText(article.getArticleNo())) {
				continue;
			}
			if (lawDataRepository.existsByLawIdAndArticleNo(lawId, article.getArticleNo())) {
				skippedDuplicates++;
				continue;
			}

			String content = LawContentBuilder.buildContent(article);
			if (!StringUtils.hasText(content)) {
				continue;
			}

			LawData entity = new LawData();
			entity.setLawId(lawId);
			entity.setLawName(lawName);
			entity.setArticleNo(article.getArticleNo());
			entity.setArticleTitle(article.getArticleTitle());
			entity.setContent(content);
			entities.add(entity);
		}

		if (skippedDuplicates > 0) {
			log.info("Skipped duplicate articles: {}", skippedDuplicates);
		}
		return entities;
	}
}
