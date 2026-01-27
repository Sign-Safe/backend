package com.example.signsafe.repository;

import com.example.signsafe.entity.ContractAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContractAnalysisRepository extends JpaRepository<ContractAnalysis, Long> {
    List<ContractAnalysis> findByGuestUuid(String guestUuid);
}