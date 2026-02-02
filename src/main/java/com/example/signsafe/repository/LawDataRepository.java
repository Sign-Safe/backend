package com.example.signsafe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.signsafe.entity.LawData;

/**
 * law_data 테이블에 대한 기본 CRUD 리포지토리.
 */
public interface LawDataRepository extends JpaRepository<LawData, Long> {
}
