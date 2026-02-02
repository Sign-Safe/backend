package com.example.signsafe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.signsafe.dto.lawgov.ArticleDto;
import com.example.signsafe.dto.lawgov.ParagraphDto;
import com.example.signsafe.dto.lawgov.SubItemDto;

class LawContentBuilderTest {
	@Test
	void buildContentStripsHtmlAndConcats() {
		ArticleDto article = new ArticleDto();
		article.setArticleContent("<p>조문내용</p>");

		SubItemDto subItem = new SubItemDto();
		subItem.setSubItemContent("<b>호내용</b>");

		ParagraphDto paragraph = new ParagraphDto();
		paragraph.setParagraphContent("항내용<br>추가");
		paragraph.setSubItems(List.of(subItem));

		article.setParagraphs(List.of(paragraph));

		String content = LawContentBuilder.buildContent(article);
		assertEquals("조문내용 항내용 추가 호내용", content);
	}
}
