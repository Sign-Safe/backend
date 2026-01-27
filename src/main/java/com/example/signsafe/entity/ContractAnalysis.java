package com.example.signsafe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 비회원 식별용 고유 ID (Guest UUID)
    private String guestUuid;

    // 계약서 파일 이름이나 제목
    private String contractTitle;

    // 제미나이가 분석한 결과 (독소조항 내용)
    @Column(columnDefinition = "TEXT")
    private String analysisResult;

    // 분석 날짜
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}