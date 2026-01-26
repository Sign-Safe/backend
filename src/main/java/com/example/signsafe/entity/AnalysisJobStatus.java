package com.example.signsafe.entity;

/**
 * 분석 작업(AnalysisJob)의 진행 상태입니다.
 */
public enum AnalysisJobStatus {
	/** 작업이 생성되어 대기 중 */
	QUEUED,
	/** 작업이 실행 중 */
	RUNNING,
	/** 작업이 정상 완료 */
	DONE,
	/** 작업이 실패 */
	FAILED
}
