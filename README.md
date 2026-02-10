# Unified Biztool

Meta(Facebook) 광고 플랫폼과 통합되는 비즈니스 관리 도구입니다. Excel 기반 대량 광고 업로드, 캠페인/세트 조회 및 생성, 광고 미디어 파일(Creative 요소) 처리 등의 기능을 제공합니다.

## 기술 스택

- Java 17
- Spring Boot 3.3.10
- Spring MVC / Spring WebFlux (`WebClient`)
- Thymeleaf
- Redis 7.2
    - Lettuce
    - Redisson
- Apache POI
- Meta Graph API v22.0

## 주요 기능

- **Excel 파일 기반 광고 데이터 일괄 업로드**
- **Meta 리소스 조회 및 보조 생성**
    - 광고 계정, 캠페인, 광고 세트, 페이지, 픽셀 조회/생성 로직 포함
- **Redis 캐싱을 통한 외부 API 호출 최소화**

## 실행 방법

### 사전 요구사항

- Java 17+
- Docker

### [환경 변수](./environment)

로컬 실행 시 `.env` 또는 쉘 환경 변수로 주입해 사용하세요.

```bash
AES_PASSWORD=;AES_SALT=;SERVER_PORT=8080;REDIS_HOST=;REDIS_PORT=6379;REDIS_PASSWORD=;APP_PASSWORD=;
```

### 실행

```bash
docker-compose up -d
./gradlew bootRun
```


애플리케이션 접속: <http://localhost:8080>

## 주요 화면/경로

### 페이지 라우트

- `/` : 메인 페이지
- `/setting` : Meta 토큰 등록 페이지
- `/upload-page` : Excel 업로드/광고 발행 페이지
- `/login` : 로그인 페이지

## 유의사항

- 사용 전 반드시 /setting에서 Meta Graph API에서 발급받은 토큰을 등록해주세요.
- 광고는 집행 대기 상태로 등록됩니다.

## 법적 책임

- 광고 소재(이미지/영상/문구) 및 랜딩 페이지의 적법성, 저작권, 초상권, 상표권 등에 대한 책임은 전적으로 광고주에게 있음
- 광고 심사 반려, 계정 제한, 페이지 제한, BM(비즈니스 매니저) 제한 등 메타 정책 위반으로 발생하는 모든 불이익은 광고주 책임임
- API 호출 과정에서 발생하는 메타 플랫폼 측의 오류, 지연, 일시적 장애로 인한 집행 실패/중복 등록/예산 소진 등은 개발자가 책임지지 않음
- 광고 성과(전환, ROAS, CPM, CTR 등) 및 마케팅 결과에 대해 개발자는 어떠한 보장도 하지 않음
- 광고 계정/픽셀/카탈로그/도메인/페이지 권한 설정 미비로 인한 문제는 광고주 책임이며, 개발자는 설정 지원 범위를 초과한 운영 책임을 지지 않음
- 광고주가 제공한 엑셀 데이터(캠페인명, 예산, 타겟, 소재 ID 등)의 오류로 인해 발생하는 모든 결과는 광고주 책임임
- 개발자는 시스템이 생성한 결과를 “자동화된 등록 요청 수행” 수준으로만 제공하며, 광고 운영 판단 및 최종 검수 의무는 광고주에게 있음