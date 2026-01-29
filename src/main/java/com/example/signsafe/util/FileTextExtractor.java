package com.example.signsafe.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Component
public class FileTextExtractor {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            return tika.parseToString(input);
        } catch (Exception ex) {
            throw new IllegalStateException("파일 텍스트 추출에 실패했습니다.", ex);
        }
    }
}
