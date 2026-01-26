package com.example.signsafe.repository;

import com.example.signsafe.entity.AnalysisJob;
import com.example.signsafe.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * AnalysisJob(분석 작업) 엔티티에 대한 DB 접근 레이어입니다.
 *
 * <p>문서(Document) 하나에 분석 요청을 여러 번 할 수 있으므로,
 * "문서별 최신 분석 작업"을 조회하는 쿼리를 하나 추가해둔 상태입니다.</p>
 */
public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, Long> {

	/**
	 * 특정 Document에 대한 가장 최신 AnalysisJob을 가져옵니다.
	 *
	 * <p>메서드 이름 규칙(Spring Data JPA Query Method)을 이용해
	 * SQL을 직접 작성하지 않고도 자동으로 쿼리를 생성합니다.</p>
	 */
	Optional<AnalysisJob> findTopByDocumentOrderByCreatedAtDesc(Document document);
}
