package com.example.signsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 계약서 원문을 저장하는 엔티티입니다.
 *
 * <p>사용자가 텍스트를 붙여넣거나 파일을 업로드하면, 최종적으로 "분석에 사용할 원문 텍스트"를
 * {@code rawText}에 저장합니다.</p>
 *
 * <p>파일 업로드인 경우 원본 파일 관련 메타데이터(filename/contentType/storagePath)도 함께 저장합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "documents")
public class Document {
	/** DB PK */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 멀티유저를 고려한 사용자 식별자(현재는 옵션) */
	@Column(name = "user_id", length = 128)
	private String userId;

	/** 입력 방식(TEXT: 직접 입력, FILE: 파일 업로드) */
	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false, length = 16)
	private DocumentSourceType sourceType;

	/** 업로드 파일명(FILE일 때) */
	@Column(length = 512)
	private String filename;

	/** 업로드 파일 content-type(FILE일 때) */
	@Column(name = "content_type", length = 128)
	private String contentType;

	/** 로컬 디스크(S3 등) 저장 경로/키(FILE일 때) */
	@Column(name = "storage_path", length = 1024)
	private String storagePath;

	/** 분석에 사용되는 원문 텍스트 */
	@Lob
	@Column(name = "raw_text")
	private String rawText;

	/** 생성 시각 */
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) createdAt = Instant.now();
	}
}
