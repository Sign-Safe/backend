package com.example.signsafe.repository;

import com.example.signsafe.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Contract 엔티티 레포지토리.
public interface ContractRepository extends JpaRepository<Contract, Long> {
    // 소유 UUID로 계약서 목록 조회.
    List<Contract> findByOwnerUuid(String ownerUuid);

    // 제목 키워드로 계약서 검색.
    List<Contract> findByTitleContainingIgnoreCase(String title);
}
