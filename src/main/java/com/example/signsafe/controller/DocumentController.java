package com.example.signsafe.controller;

import com.example.signsafe.dto.*;
import com.example.signsafe.entity.AnalysisJob;
import com.example.signsafe.entity.Document;
import com.example.signsafe.service.AnalysisJobService;
import com.example.signsafe.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * "문서(Document) 생성" 관련 API 컨트롤러입니다.
 *
 * <p>프론트엔드가 계약서 텍스트를 직접 보내거나, 파일(PDF/DOCX/TXT)을 업로드하면
 * 1) 백엔드가 텍스트를 확보하고(DocumentService)
 * 2) DB에 Document를 저장한 뒤
 * 3) 분석 작업을 요청(AnalysisJobService)하는 역할을 합니다.</p>
 *
 * <p>여기서는 "분석 결과"를 바로 반환하지 않고, AnalysisJob을 생성해서 비동기로 분석을 돌립니다.
 * 프론트는 응답으로 받은 documentId로 status/result API를 폴링하면 됩니다.</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentService documentService;
	private final AnalysisJobService analysisJobService;

	/**
	 * 텍스트로 문서를 생성합니다.
	 *
	 * <p>요청: { text, userId? }
	 * 응답: { documentId, analysisJobId }</p>
	 */
	@PostMapping("/documents/text")
	public CreateDocumentResponse createFromText(@Valid @RequestBody CreateDocumentFromTextRequest request) {
		Document doc = documentService.createFromText(request.getUserId(), request.getText());
		AnalysisJob job = analysisJobService.requestAnalysis(doc.getId());
		return new CreateDocumentResponse(doc.getId(), job.getId());
	}

	/**
	 * 파일 업로드로 문서를 생성합니다.
	 *
	 * <p>multipart/form-data로 file을 받아서
	 * 1) 로컬 디스크에 저장하고
	 * 2) 파일에서 텍스트를 추출한 뒤
	 * 3) Document 저장 + 분석 job 요청까지 수행합니다.</p>
	 */
	@PostMapping(value = "/documents/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CreateDocumentResponse createFromFile(
			@RequestPart("file") MultipartFile file,
			@RequestParam(value = "userId", required = false) String userId
	) {
		Document doc = documentService.createFromFile(userId, file);
		AnalysisJob job = analysisJobService.requestAnalysis(doc.getId());
		return new CreateDocumentResponse(doc.getId(), job.getId());
	}
}
