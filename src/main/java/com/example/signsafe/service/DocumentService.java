package com.example.signsafe.service;

import com.example.signsafe.entity.Document;
import com.example.signsafe.entity.DocumentSourceType;
import com.example.signsafe.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Document(계약서 원문) 생성/저장에 대한 비즈니스 로직을 담당합니다.
 *
 * <p>컨트롤러는 "HTTP 처리"만 하고, 실제로 Document 엔티티를 만들고 DB에 저장하는 책임은
 * 이 서비스가 갖도록 분리해둔 구조입니다.</p>
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

	private final DocumentRepository documentRepository;
	private final StorageService storageService;
	private final TextExtractionService textExtractionService;

	/**
	 * 사용자가 붙여넣은 텍스트로 Document를 생성합니다.
	 */
	@Transactional
	public Document createFromText(String userId, String text) {
		// 1) 엔티티 구성
		Document doc = Document.builder()
				.userId(userId)
				.sourceType(DocumentSourceType.TEXT)
				.rawText(text == null ? "" : text.trim())
				.build();
		// 2) DB 저장
		return documentRepository.save(doc);
	}

	/**
	 * 파일 업로드로 Document를 생성합니다.
	 *
	 * <p>현재 구현은
	 * 1) 파일을 로컬 디스크에 저장하고(storageService)
	 * 2) 파일에서 텍스트를 추출한 뒤(textExtractionService)
	 * 3) Document에 원문(rawText)로 저장합니다.</p>
	 */
	@Transactional
	public Document createFromFile(String userId, MultipartFile file) {
		// 1) 파일 저장(원본 파일 추후 다운로드/감사용)
		String storagePath = storageService.store(file);
		// 2) 텍스트 추출(분석의 입력)
		String extractedText = textExtractionService.extractText(file);

		// 3) 엔티티 구성 + DB 저장
		Document doc = Document.builder()
				.userId(userId)
				.sourceType(DocumentSourceType.FILE)
				.filename(file.getOriginalFilename())
				.contentType(file.getContentType())
				.storagePath(storagePath)
				.rawText(extractedText)
				.build();
		return documentRepository.save(doc);
	}
}
