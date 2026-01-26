package com.example.signsafe.entity;

/**
 * 문서(Document)가 생성된 경로/입력 방식입니다.
 */
public enum DocumentSourceType {
	/** 사용자가 텍스트를 직접 붙여넣은 경우 */
	TEXT,
	/** 사용자가 파일을 업로드한 경우 */
	FILE
}
