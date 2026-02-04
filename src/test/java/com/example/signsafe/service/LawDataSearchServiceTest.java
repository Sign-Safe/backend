package com.example.signsafe.service;

import com.example.signsafe.repository.LawDataRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LawDataSearchServiceTest {

	@Test
	void extractKeywords_picksCandidatesAndArticles() {
		LawDataRepository repository = mock(LawDataRepository.class);
		LawDataSearchService service = new LawDataSearchService(repository);

		String text = "본 계약은 제 3 조 및 제4조에 따라 해지와 위약금을 규정한다.";
		List<String> keywords = service.extractKeywords(text);

		assertThat(keywords).contains("해지", "위약금", "제3조", "제4조");
	}
}
