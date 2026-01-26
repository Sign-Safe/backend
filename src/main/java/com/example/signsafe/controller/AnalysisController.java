package com.example.signsafe.controller;

import com.example.signsafe.dto.AnalysisStatusResponse;
import com.example.signsafe.entity.AnalysisJob;
import com.example.signsafe.service.AnalysisJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * "분석(Analysis)" 작업 관련 API 컨트롤러입니다.
 *
 * <p>키 포인트는 분석을 동기로 끝내지 않고 "AnalysisJob"이라는 작업을 만들어서
 * 비동기(@Async)로 돌린다는 점입니다.</p>
 *
 * <ul>
 *   <li>start: 특정 documentId로 분석 job을 새로 만들고 실행</li>
 *   <li>status: 해당 documentId의 최신 job 상태(QUEUED/RUNNING/DONE/FAILED) 조회</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

	private final AnalysisJobService analysisJobService;

	/**
	 * 분석을 시작(분석 job 생성 + 비동기 실행 트리거)합니다.
	 */
	@PostMapping("/analysis/{documentId}")
	public AnalysisStatusResponse start(@PathVariable Long documentId) {
		AnalysisJob job = analysisJobService.requestAnalysis(documentId);
		return new AnalysisStatusResponse(
				documentId,
				job.getId(),
				job.getStatus(),
				job.getStartedAt(),
				job.getFinishedAt(),
				job.getErrorMessage()
		);
	}

	/**
	 * 특정 문서(documentId)의 "가장 최신" 분석 job 상태를 조회합니다.
	 */
	@GetMapping("/analysis/{documentId}/status")
	public AnalysisStatusResponse status(@PathVariable Long documentId) {
		AnalysisJob job = analysisJobService.getLatestForDocument(documentId);
		return new AnalysisStatusResponse(
				documentId,
				job.getId(),
				job.getStatus(),
				job.getStartedAt(),
				job.getFinishedAt(),
				job.getErrorMessage()
		);
	}
}
