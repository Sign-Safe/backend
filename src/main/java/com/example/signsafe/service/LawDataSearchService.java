package com.example.signsafe.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.signsafe.entity.LawData;
import com.example.signsafe.repository.LawDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LawDataSearchService {
	private static final int MAX_KEYWORDS = 8;
	private static final int PER_KEYWORD_LIMIT = 5;
	private static final int MAX_RESULTS = 8;
	private static final Pattern ARTICLE_PATTERN = Pattern.compile("제\\s*\\d+\\s*조");

	private static final List<String> KEYWORD_CANDIDATES = List.of(
			"계약",
			"약관",
			"해지",
			"해제",
			"갱신",
			"위약금",
			"손해배상",
			"배상",
			"책임",
			"면책",
			"비밀유지",
			"개인정보",
			"분쟁",
			"관할",
			"준거법"
	);

	private final LawDataRepository lawDataRepository;

	public LawContextResult buildLawContext(String contractText) {
		List<String> keywords = extractKeywords(contractText);
		if (keywords.isEmpty()) {
			return new LawContextResult("", List.of(), List.of());
		}

		Map<String, LawData> unique = new LinkedHashMap<>();
		for (String keyword : keywords) {
			List<LawData> items = lawDataRepository.searchByKeyword(keyword, PageRequest.of(0, PER_KEYWORD_LIMIT));
			for (LawData item : items) {
				String key = item.getLawId() + ":" + item.getArticleNo();
				unique.putIfAbsent(key, item);
				if (unique.size() >= MAX_RESULTS) {
					break;
				}
			}
			if (unique.size() >= MAX_RESULTS) {
				break;
			}
		}

		if (unique.isEmpty()) {
			return new LawContextResult("", keywords, List.of());
		}

		List<String> snippets = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (LawData item : unique.values()) {
			String snippet = formatLawData(item);
			snippets.add(snippet);
			if (builder.length() > 0) {
				builder.append('\n');
			}
			builder.append(snippet);
		}
		return new LawContextResult(builder.toString(), keywords, snippets);
	}

	List<String> extractKeywords(String contractText) {
		if (!StringUtils.hasText(contractText)) {
			return List.of();
		}

		Set<String> keywords = new LinkedHashSet<>();
		for (String candidate : KEYWORD_CANDIDATES) {
			if (contractText.contains(candidate)) {
				keywords.add(candidate);
				if (keywords.size() >= MAX_KEYWORDS) {
					return new ArrayList<>(keywords);
				}
			}
		}

		Matcher matcher = ARTICLE_PATTERN.matcher(contractText);
		while (matcher.find() && keywords.size() < MAX_KEYWORDS) {
			String raw = matcher.group();
			String normalized = raw.replaceAll("\\s+", "");
			if (!normalized.isBlank()) {
				keywords.add(normalized);
			}
		}

		return new ArrayList<>(keywords);
	}

	private String formatLawData(LawData item) {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.hasText(item.getLawName())) {
			builder.append(item.getLawName()).append(' ');
		}
		if (StringUtils.hasText(item.getArticleNo())) {
			builder.append(item.getArticleNo()).append(' ');
		}
		if (StringUtils.hasText(item.getArticleTitle())) {
			builder.append(item.getArticleTitle()).append(": ");
		}
		builder.append(item.getContent());
		return builder.toString().trim();
	}

	record LawContextResult(String context, List<String> keywords, List<String> snippets) {
	}
}
