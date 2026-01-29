package com.example.signsafe.repository;

import com.example.signsafe.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// AnalysisResult 엔티티 레포지토리.
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    // 분석 작업 ID로 결과 조회.
    Optional<AnalysisResult> findByAnalysisJobId(Long analysisJobId);

    // 계약서 ID로 결과 조회(분석 작업 경유).
    Optional<AnalysisResult> findByAnalysisJobContractId(Long contractId);
}
