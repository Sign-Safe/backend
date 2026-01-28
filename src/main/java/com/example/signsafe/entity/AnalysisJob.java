package com.example.signsafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

// 계약서 분석 요청 엔티티.
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "analysis_jobs")
public class AnalysisJob extends BaseTimeEntity {
    // 기본 키.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 분석 대상 계약서.
    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // 분석 상태.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisJobStatus status = AnalysisJobStatus.PENDING;

    // 분석 요청 시각.
    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    // 분석 완료 시각.
    @Column(name = "completed_at")
    private Instant completedAt;

    // 실패 사유 또는 오류 메시지.
    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

}
