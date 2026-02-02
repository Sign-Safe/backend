package com.example.signsafe.service;

import com.example.signsafe.dto.lawgov.ArticleDto;
import com.example.signsafe.dto.lawgov.ParagraphDto;
import com.example.signsafe.dto.lawgov.SubItemDto;

/**
 * 조문/항/호 내용을 합쳐 AI 검색용 텍스트를 만드는 유틸리티.
 */
public final class LawContentBuilder {
	private LawContentBuilder() {
	}

	public static String buildContent(ArticleDto article) {
		// 조문 본문을 먼저 추가한 뒤, 항/호 내용을 순서대로 덧붙인다.
		StringBuilder builder = new StringBuilder();
		appendClean(builder, article.getArticleContent());

		if (article.getParagraphs() != null) {
			for (ParagraphDto paragraph : article.getParagraphs()) {
				appendClean(builder, paragraph.getParagraphContent());
				if (paragraph.getSubItems() != null) {
					for (SubItemDto subItem : paragraph.getSubItems()) {
						appendClean(builder, subItem.getSubItemContent());
					}
				}
			}
		}

		return builder.toString().trim();
	}

	private static void appendClean(StringBuilder builder, String value) {
		// HTML을 제거하고 비어있지 않을 때만 추가
		String cleaned = stripHtml(value);
		if (cleaned.isBlank()) {
			return;
		}
		if (builder.length() > 0) {
			builder.append(' ');
		}
		builder.append(cleaned);
	}

	static String stripHtml(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		// 태그를 공백으로 치환 후 연속 공백을 하나로 정리
		String withoutTags = value.replaceAll("<[^>]*>", " ");
		return withoutTags.replaceAll("\\s+", " ").trim();
	}
}
