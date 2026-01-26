package com.example.signsafe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

/**
 * {@link StorageService}의 1차 구현체: 로컬 디스크에 파일을 저장합니다.
 *
 * <p>저장 경로는 application.properties의 {@code app.storage.local.base-dir}로 설정합니다.
 * 파일명은 충돌 방지를 위해 UUID를 prefix로 붙입니다.</p>
 */
@Service
public class LocalStorageService implements StorageService {

	/**
	 * 업로드 파일을 저장할 기본 디렉토리(절대경로로 정규화).
	 */
	private final Path baseDir;

	public LocalStorageService(@Value("${app.storage.local.base-dir:./data/uploads}") String baseDir) {
		this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
	}

	/**
	 * 파일을 baseDir 밑에 저장하고 저장된 파일의 경로(String)를 반환합니다.
	 */
	@Override
	public String store(MultipartFile file) {
		try {
			Files.createDirectories(baseDir);

			// 원본 파일명은 안전하게 정리(특수문자 제거)
			String original = file.getOriginalFilename();
			String safeName = (original == null || original.isBlank())
					? "upload"
					: original.replaceAll("[^a-zA-Z0-9._-]", "_");

			// UUID를 붙여 충돌 방지
			String filename = UUID.randomUUID() + "_" + safeName;
			Path target = baseDir.resolve(filename);

			try (InputStream in = file.getInputStream()) {
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
			return target.toString();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to store file", e);
		}
	}
}
