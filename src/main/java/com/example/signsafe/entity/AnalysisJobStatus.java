package com.example.signsafe.entity;

// 분석 요청 상태 흐름.
public enum AnalysisJobStatus {
    // 생성됨(미시작).
    PENDING,
    // 분석 진행 중.
    RUNNING,
    // 분석 완료.
    DONE,
    // 분석 실패.
    FAILED
}
