package com.example.signsafe.controller;

import com.example.signsafe.dto.AnalysisResponse;
import com.example.signsafe.dto.TextAnalysisRequest;
import com.example.signsafe.service.GeminiService;
import com.example.signsafe.util.FileTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final GeminiService geminiService;
    private final FileTextExtractor fileTextExtractor;

    @PostMapping("/text")
    public AnalysisResponse analyzeText(@RequestBody TextAnalysisRequest request) {
        if (request == null || request.text() == null || request.text().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "분석할 텍스트가 비어 있습니다.");
        }
        return runAnalysis(request.text(), request.uuid(), request.title());
    }

    @PostMapping("/file")
    public AnalysisResponse analyzeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uuid") String uuid
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드된 파일이 없습니다.");
        }
        String extracted = fileTextExtractor.extractText(file);
        if (extracted.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일에서 텍스트를 추출하지 못했습니다.");
        }
        String title = file.getOriginalFilename();
        return runAnalysis(extracted, uuid, title);
    }

    private AnalysisResponse runAnalysis(String text, String uuid, String title) {
        try {
            return geminiService.analyzeText(text, uuid, title);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "분석 중 오류가 발생했습니다.",
                    ex
            );
        }
    }
}
