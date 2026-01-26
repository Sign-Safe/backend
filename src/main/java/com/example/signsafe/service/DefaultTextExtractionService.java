package com.example.signsafe.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * {@link TextExtractionService}의 기본 구현체입니다.
 *
 * <p>파일 확장자/콘텐츠 타입으로 PDF/DOCX/TXT를 판별해 아래 라이브러리로 텍스트를 뽑습니다.</p>
 * <ul>
 *   <li>PDF: Apache PDFBox</li>
 *   <li>DOCX: Apache POI</li>
 *   <li>TXT: UTF-8로 그대로 읽기</li>
 * </ul>
 *
 * <p>요구사항대로 전처리는 최소화하고, 줄바꿈 통일/탭 제거 정도만 수행합니다.</p>
 */
@Slf4j
@Service
public class DefaultTextExtractionService implements TextExtractionService {

	/**
	 * 파일 타입을 판단해서 적절한 추출 로직으로 분기합니다.
	 */
	@Override
	public String extractText(MultipartFile file) {
		String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();

		try {
			if (filename.endsWith(".pdf") || contentType.contains("pdf")) {
				return extractPdf(file);
			}
			if (filename.endsWith(".docx") || contentType.contains("wordprocessingml")) {
				return extractDocx(file);
			}
			// Default: treat as text
			return extractTxt(file);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to extract text from file", e);
		}
	}

	/**
	 * PDF에서 텍스트 추출(PDFBox).
	 */
	private String extractPdf(MultipartFile file) throws IOException {
		try (InputStream in = file.getInputStream()) {
			// PDFBox 3.x는 InputStream 직접 load가 제한되어 byte[] 로딩 방식 사용
			byte[] bytes = in.readAllBytes();
			try (PDDocument doc = Loader.loadPDF(bytes)) {
				PDFTextStripper stripper = new PDFTextStripper();
				String text = stripper.getText(doc);
				return normalize(text);
			}
		}
	}

	/**
	 * DOCX에서 텍스트 추출(Apache POI).
	 */
	private String extractDocx(MultipartFile file) throws IOException {
		try (InputStream in = file.getInputStream(); XWPFDocument doc = new XWPFDocument(in); XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
			String text = ex.getText();
			return normalize(text);
		}
	}

	/**
	 * 일반 텍스트 파일(TXT)로 간주하고 UTF-8로 읽습니다.
	 *
	 * <p>주의: 실제 서비스에서 인코딩이 다양하면(UTF-8이 아닐 때)
	 * 인코딩 감지 로직을 추가할 수 있습니다.</p>
	 */
	private String extractTxt(MultipartFile file) throws IOException {
		try (InputStream in = file.getInputStream()) {
			byte[] bytes = in.readAllBytes();
			String text = new String(bytes, StandardCharsets.UTF_8);
			return normalize(text);
		}
	}

	/**
	 * 1차: 전처리 최소화(요구사항)
	 * - CRLF/LF 통일
	 * - 연속 공백 정리(너무 공격적이지 않게)
	 */
	private String normalize(String text) {
		if (text == null) return "";
		String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
		// 문단 구분을 망치지 않도록, 공백은 과도하게 제거하지 않고 탭만 공백으로
		normalized = normalized.replace('\t', ' ');
		return normalized.trim();
	}
}
