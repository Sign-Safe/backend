package com.example.signsafe.service;

import com.example.signsafe.entity.*;
import com.example.signsafe.repository.AnalysisJobRepository;
import com.example.signsafe.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 분석 작업(AnalysisJob)의 생성/실행/상태 변경을 담당하는 서비스입니다.
 *
 * <p>핵심 개념:</p>
 * <ul>
 *   <li>Document: 계약서 "원문"(텍스트)</li>
 *   <li>AnalysisJob: 그 문서를 분석하는 "작업"</li>
 * </ul>
 *
 * <p>분석은 시간이 오래 걸릴 수 있어 비동기(@Async)로 실행합니다.
 * 현재는 OpenAI 호출 전 단계라, 실제 분석 대신 잠깐 sleep 후 DONE 처리만 합니다.
 * (나중에 B 담당이 OpenAI 호출 + 결과 저장 로직으로 대체)</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisJobService {

	private final AnalysisJobRepository analysisJobRepository;
	private final DocumentRepository documentRepository;

	/**
	 * 특정 문서(documentId)에 대해 분석 작업을 요청합니다.
	 *
	 * <p>흐름:</p>
	 * <ol>
	 *   <li>Document 조회</li>
	 *   <li>AnalysisJob을 QUEUED로 생성/저장</li>
	 *   <li>비동기 실행 트리거(runAsync)</li>
	 * </ol>
	 */
	@Transactional
	public AnalysisJob requestAnalysis(Long documentId) {
		Document doc = documentRepository.findById(documentId)
				.orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

		AnalysisJob job = AnalysisJob.builder()
				.document(doc)
				.status(AnalysisJobStatus.QUEUED)
				.model("TBD")
				.build();
		job = analysisJobRepository.save(job);

		// fire-and-forget: 요청 스레드를 막지 않고 백그라운드에서 처리
		runAsync(job.getId());
		return job;
	}

	/**
	 * 실제 분석 수행(비동기).
	 *
	 * <p>@Async("analysisExecutor") 설정으로 별도의 스레드풀에서 실행됩니다.</p>
	 */
	@Async("analysisExecutor")
	public void runAsync(Long jobId) {
		// NOTE: 1차 스캐폴딩 - 실제 OpenAI 호출은 B 담당 모듈과 연결 예정
		try {
			markRunning(jobId);

			// TODO(B 담당 연결): 여기서 Document.rawText를 가져와 OpenAI 분석 호출 + 결과 저장
			Thread.sleep(200);

			markDone(jobId);
		} catch (Exception e) {
			log.error("Analysis job failed: {}", jobId, e);
			markFailed(jobId, e.getMessage());
		}
	}

	/**
	 * 특정 문서(documentId)의 "최신" AnalysisJob을 가져옵니다.
	 */
	@Transactional
	public AnalysisJob getLatestForDocument(Long documentId) {
		Document doc = documentRepository.findById(documentId)
				.orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
		return analysisJobRepository.findTopByDocumentOrderByCreatedAtDesc(doc)
				.orElseThrow(() -> new IllegalArgumentException("No analysis job for document: " + documentId));
	}

	/**
	 * job 상태를 RUNNING으로 변경하고 startedAt을 기록합니다.
	 *
	 * <p>주의: 같은 클래스 내부에서 @Transactional 메서드를 직접 호출하면(Spring AOP 프록시 우회)
	 * 트랜잭션이 안 걸릴 수 있어서, 여기서는 repository를 통해 명시적으로 상태를 갱신합니다.</p>
	 */
	protected void markRunning(Long jobId) {
		AnalysisJob job = analysisJobRepository.findById(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
		job.setStatus(AnalysisJobStatus.RUNNING);
		job.setStartedAt(Instant.now());
		analysisJobRepository.save(job);
	}

	/**
	 * job 상태를 DONE으로 변경하고 finishedAt을 기록합니다.
	 */
	protected void markDone(Long jobId) {
		AnalysisJob job = analysisJobRepository.findById(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
		job.setStatus(AnalysisJobStatus.DONE);
		job.setFinishedAt(Instant.now());
		analysisJobRepository.save(job);
	}

	/**
	 * job 상태를 FAILED로 변경하고 finishedAt/errorMessage를 기록합니다.
	 */
	protected void markFailed(Long jobId, String message) {
		AnalysisJob job = analysisJobRepository.findById(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
		job.setStatus(AnalysisJobStatus.FAILED);
		job.setFinishedAt(Instant.now());
		job.setErrorMessage(message);
		analysisJobRepository.save(job);
	}
}
