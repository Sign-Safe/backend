package com.example.signsafe.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.signsafe.entity.LawData;

/**
 * law_data 테이블에 대한 기본 CRUD 리포지토리.
 */
public interface LawDataRepository extends JpaRepository<LawData, Long> {
	boolean existsByLawIdAndArticleNo(String lawId, String articleNo);

	@Query("""
			select l
			from LawData l
			where l.content like concat('%', :keyword, '%')
				or lower(l.lawName) like lower(concat('%', :keyword, '%'))
				or lower(l.articleTitle) like lower(concat('%', :keyword, '%'))
				or lower(l.articleNo) like lower(concat('%', :keyword, '%'))
			""")
	List<LawData> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
