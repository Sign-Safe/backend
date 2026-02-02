package com.example.signsafe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * law_data 테이블에 저장되는 조문 단위 데이터 엔티티.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "law_data")
public class LawData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 법령 고유 ID
	@Column(name = "law_id", nullable = false, length = 50)
	private String lawId;

	// 법령명(한글)
	@Column(name = "law_name", nullable = false, length = 200)
	private String lawName;

	// 조문 번호
	@Column(name = "article_no", length = 50)
	private String articleNo;

	// 조문 제목
	@Column(name = "article_title", length = 200)
	private String articleTitle;

	// AI 검색용 텍스트(조문 + 모든 항/호 내용을 합친 결과)
	@Lob
	@Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
	private String content;
}
