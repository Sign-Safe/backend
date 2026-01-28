package com.example.signsafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.Instant;

// 엔티티 공통 생성/수정 시각을 자동 관리하는 베이스 클래스.
@MappedSuperclass
@Getter
public abstract class BaseTimeEntity {
    // 생성 시각(UTC).
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 수정 시각(UTC).
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // 최초 저장 전에 생성/수정 시각을 설정.
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // 갱신 시마다 수정 시각을 갱신.
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
