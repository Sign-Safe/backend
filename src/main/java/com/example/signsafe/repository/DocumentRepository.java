package com.example.signsafe.repository;

import com.example.signsafe.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Document 엔티티에 대한 DB 접근 레이어입니다.
 *
 * <p>Spring Data JPA가 구현체를 자동 생성해서 아래 같은 메서드를 기본 제공해줍니다:</p>
 * <ul>
 *   <li>save</li>
 *   <li>findById</li>
 *   <li>findAll</li>
 *   <li>deleteById</li>
 * </ul>
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
