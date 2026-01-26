# Signsafe Backend

계약서(문서) 텍스트/파일을 업로드 받아 **원문 텍스트를 저장**하고,
비동기로 **분석 Job(작업)** 을 생성/상태 조회까지 제공하는 Spring Boot 백엔드입니다.

> 현재 단계: OpenAI 분석은 아직 연결 전(스캐폴딩). 분석 Job은 생성 후 짧게 대기했다가 DONE으로 바뀌는 구조입니다.

---

## 1) 큰 흐름(한 줄 요약)

1. 프론트가 텍스트 또는 파일(PDF/DOCX/TXT)을 업로드
2. 백엔드가 `Document`를 DB에 저장(원문 텍스트는 `rawText`)
3. `AnalysisJob`을 생성하고 비동기 실행
4. 프론트는 `status` API로 상태를 폴링

---

## 2) 실행 환경

- Java 17
- Spring Boot 3.5.x
- DB: MySQL (로컬 설치된 MySQL 사용)
- Swagger: springdoc-openapi

---

## 3) 로컬 MySQL 설정

`src/main/resources/application.properties`에서 아래 값을 본인 환경에 맞게 설정하세요.

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

현재 기본값(예시):
- DB: `signsafe`
- username: `root`
- password: `0000`

> 중요: Flyway는 사용하지 않습니다.
> 스키마(테이블)는 `spring.jpa.hibernate.ddl-auto=update` 설정에 의해 JPA(Hibernate)가 자동 생성/업데이트합니다.

---

## 4) 실행 방법

### 4-1. 테스트 실행(권장)
테스트는 MySQL 없이도 돌아가게 H2 인메모리 DB를 사용합니다.

```powershell
cd C:\Coding_study\signsafe\backend
.\gradlew.bat clean test --no-daemon
```

### 4-2. 서버 실행

```powershell
cd C:\Coding_study\signsafe\backend
.\gradlew.bat bootRun --no-daemon
```

서버가 뜨면 기본 포트는 `8080` 입니다.

---

## 5) Swagger(스웨거) URL

서버 실행 후 아래 주소로 접속합니다.

- Swagger UI
  - `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON
  - `http://localhost:8080/v3/api-docs`

포트를 바꿨다면(`server.port=8081` 등) 포트만 바꿔서 접속하면 됩니다.

---

## 6) 주요 API

### 6-1) 텍스트로 문서 생성
- `POST /api/documents/text`

요청(JSON):
```json
{
  "text": "계약서 내용...",
  "userId": "user-1"
}
```

응답(JSON):
```json
{
  "documentId": 1,
  "analysisJobId": 10
}
```

### 6-2) 파일로 문서 생성
- `POST /api/documents/file` (multipart/form-data)
  - `file`: 업로드 파일
  - `userId`: optional

### 6-3) 분석 시작(분석 job 생성)
- `POST /api/analysis/{documentId}`

### 6-4) 분석 상태 조회(폴링)
- `GET /api/analysis/{documentId}/status`

응답 예시:
```json
{
  "documentId": 1,
  "analysisJobId": 10,
  "status": "DONE",
  "startedAt": "2026-01-27T00:00:00Z",
  "finishedAt": "2026-01-27T00:00:01Z",
  "errorMessage": null
}
```

---

## 7) 코드 구조(폴더별 역할)

- `controller/`
  - HTTP API 엔드포인트(요청/응답)
- `service/`
  - 비즈니스 로직
  - 업로드 파일 저장, 텍스트 추출, 분석 job 생성/실행
- `entity/`
  - DB 테이블과 매핑되는 JPA 엔티티
- `repository/`
  - DB 접근(JPA Repository)
- `dto/`
  - API 요청/응답용 DTO
- `config/`
  - 비동기 스레드풀 등 스프링 설정

---

## 8) 핵심 클래스(파일) 빠른 설명

- `DocumentController`
  - `/api/documents/text`, `/api/documents/file`
- `AnalysisController`
  - `/api/analysis/{documentId}`, `/status`
- `DocumentService`
  - Document 생성/저장
- `DefaultTextExtractionService`
  - PDFBox/POI로 PDF/DOCX 텍스트 추출
- `AnalysisJobService`
  - AnalysisJob 생성/비동기 실행(현재는 sleep 후 DONE)

---

## 9) 자주 겪는 문제(트러블슈팅)

### 9-1) "Port 8080 was already in use"
8080을 이미 쓰는 프로세스가 있어서 서버가 못 뜨는 경우입니다.

PID 확인:
```powershell
netstat -ano | findstr :8080
```

PID 종료(예: 12345):
```powershell
taskkill /PID 12345 /F
```

또는 `application.properties`에 포트를 바꿔도 됩니다:
```ini
server.port=8081
```

### 9-2) MySQL 연결 실패
- MySQL 서비스가 실행 중인지 확인
- `application.properties`의 url/username/password가 맞는지 확인
- DB(`signsafe`)가 존재하는지 확인

---

## 10) 참고(개발 메모)

- 작업 중 실수로 생성된 잘못된 소스 경로(`com/example/signsafe/backend/**`)가 있으나,
  현재는 `build.gradle`에서 해당 경로를 컴파일에서 제외하도록 처리되어 있습니다.
