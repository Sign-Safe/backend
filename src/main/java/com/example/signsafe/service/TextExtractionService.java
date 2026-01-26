package com.example.signsafe.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드된 파일(PDF/DOCX/TXT 등)에서 "분석에 사용할 텍스트"를 추출하는 역할입니다.
 *
 * <p>컨트롤러/DocumentService는 파일 타입별 라이브러리(PDFBox/POI 등)를 직접 알지 않고,
 * 이 인터페이스만 호출합니다.</p>
 */
public interface TextExtractionService {
	/**
	 * @return 파일에서 추출한 텍스트(실패 시 예외 발생)
	 */
	String extractText(MultipartFile file);
}
