package com.example.signsafe.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드된 파일(원본)을 저장하는 역할의 인터페이스입니다.
 *
 * <p>왜 인터페이스로 분리했냐면:</p>
 * <ul>
 *   <li>개발 단계에서는 로컬 디스크 저장(LocalStorageService)</li>
 *   <li>운영 단계에서는 S3/MinIO 같은 오브젝트 스토리지로 교체</li>
 * </ul>
 * 처럼 구현을 바꿔끼우기 쉽도록 하기 위함입니다.
 */
public interface StorageService {
	/**
	 * 파일을 저장하고, DB에 저장 가능한 "경로"(또는 키)를 반환합니다.
	 *
	 * @return storage path (relative or absolute) that can be stored in DB.
	 */
	String store(MultipartFile file);
}
