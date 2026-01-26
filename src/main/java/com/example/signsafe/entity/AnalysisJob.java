package com.example.signsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 특정 Document를 분석하는 "작업"(Job)을 나타내는 엔티티입니다.
 *
 * <p>분석은 오래 걸릴 수 있으므로, 요청 시점에 job을 하나 만들고 상태를 추적합니다.</p>
 * <ul>
 *   <li>QUEUED: 대기</li>
 *   <li>RUNNING: 실행 중</li>
 *   <li>DONE: 완료</li>
 *   <li>FAILED: 실패</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "analysis_jobs")
public class AnalysisJob {
	/** DB PK */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 어떤 문서를 분석하는지(FK) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	/** 작업 상태 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private AnalysisJobStatus status;

	/** 사용한 모델명(추후 OpenAI 모델명 등) */
	@Column(length = 64)
	private String model;

	/** 시작 시각 */
	@Column(name = "started_at")
	private Instant startedAt;

	/** 종료 시각 */
	@Column(name = "finished_at")
	private Instant finishedAt;

	/** 실패 시 에러 메시지 */
	@Lob
	@Column(name = "error_message")
	private String errorMessage;

	/** 생성 시각 */
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) createdAt = Instant.now();
	}
}
