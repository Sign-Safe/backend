package com.example.signsafe.repository;

import com.example.signsafe.entity.AnalysisJob;
import com.example.signsafe.entity.AnalysisJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// AnalysisJob 엔티티 레포지토리.
public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, Long> {
    // 계약서 ID로 분석 작업 목록 조회.
    List<AnalysisJob> findByContractId(Long contractId);

    // 상태로 분석 작업 필터링.
    List<AnalysisJob> findByStatus(AnalysisJobStatus status);

    // 계약서별 최신 분석 작업 조회.
    Optional<AnalysisJob> findFirstByContractIdOrderByRequestedAtDesc(Long contractId);
}
