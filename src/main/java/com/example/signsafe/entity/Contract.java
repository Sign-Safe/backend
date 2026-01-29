package com.example.signsafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 계약서 엔티티.
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contracts")
public class Contract extends BaseTimeEntity {
    // 기본 키.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 쿠키 기반 소유 식별자(UUID).
    @Column(name = "owner_uuid", length = 36, nullable = false)
    private String ownerUuid;

    // 계약서 제목.
    @Column(name = "title", length = 200)
    private String title;

    // 원문 텍스트.
    @Lob
    @Column(name = "original_text")
    private String originalText;

    // 업로드 파일 경로.
    @Column(name = "file_path", length = 500)
    private String filePath;

    // 파일 유형 또는 확장자(PDF, DOCX, TXT 등).
    @Column(name = "file_type", length = 20)
    private String fileType;
}
