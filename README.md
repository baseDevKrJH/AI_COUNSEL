# AI 상담 분석 프로젝트

상담사들의 상담 내용을 분석하고 예측하는 AI 기반 상담 분석 시스템

## 1. 개발환경 구성

- [x] Spring Boot 3.x 설정
- [x] H2 Database 설정
- [x] Swagger UI 설정
- [x] REST API 기본 구조 설정

## 2. 기능 개발

### 2.1 상담 내용 분석 기능
- [x] 상담 데이터 모델 설계
- [x] 상담 내용 저장 API 개발
- [x] 상담 내용 분석 알고리즘 구현
- [x] 분석 결과 조회 API 개발

### 2.2 다음 상담 예측 기능
- [x] 상담 패턴 분석 모델 설계
- [x] 예측 알고리즘 구현
- [x] 예측 결과 API 개발

### 2.3 상담 상품 관리
- [x] 상품 데이터 모델 설계 (Counsel 엔티티에 포함)
- [x] 상담-상품 연관 관계 설계 (productInfo 필드로 구현)
- [ ] 별도 상품 관리 API 개발
- [ ] 상담별 상품 통계 기능 구현

## 3. 기술 스택

- Backend: Spring Boot 3.x
- Database: H2 Database
- API Documentation: Swagger UI
- Architecture: REST API

## 4. 프로젝트 진행 상황

- [x] 프로젝트 기본 환경 설정
  - [x] Spring Boot 3.x 설정
  - [x] H2 Database 설정
  - [x] Swagger UI 설정
- [x] 데이터베이스 스키마 설계
  - [x] 상담 엔티티 설계
- [x] API 엔드포인트 설계
  - [x] 상담 CRUD API 구현
  - [x] 상담 분석 API 구현
  - [x] 예측 API 구현
- [x] 기본 CRUD 기능 구현
- [x] 분석 알고리즘 개발
  - [x] 상담 내용 분석 알고리즘
  - [x] 다음 상담 예측 알고리즘
    - [x] 상담 패턴 분석
    - [x] 상담 주기 계산
    - [x] 키워드 빈도 분석
- [x] 테스트 케이스 작성
  - [x] 상담 분석 테스트
  - [x] 예측 알고리즘 테스트
- [x] API 문서화 (Swagger UI)
- [x] 개발환경 설정
  - [x] IDE별 .gitignore 설정 완료
  - [x] Git 저장소 초기화 및 첫 커밋 완료

## 5. 완료된 주요 기능

### ✅ 완료된 API 엔드포인트
- POST `/api/counsels` - 상담 등록
- GET `/api/counsels/{id}` - 상담 상세 조회  
- GET `/api/counsels` - 상담 목록 조회
- GET `/api/counsels/{id}/analysis` - 상담 내용 분석
- GET `/api/counsels/{customerId}/prediction` - 다음 상담 예측

### ✅ 구현된 분석 알고리즘
- **키워드 분석**: 상담 내용에서 주요 키워드 추출 및 빈도 분석
- **감정 분석**: 긍정/부정/중립 감정 점수 계산
- **주제 분류**: 상담 내용을 주제별로 자동 분류
- **패턴 분석**: 고객별 상담 패턴 및 주기 분석
- **상품 추천**: 상담 내용 기반 관련 상품 추천

### ✅ 테스트 커버리지
- CounselAnalysisServiceTest: 분석 알고리즘 단위 테스트
- CounselPredictionServiceTest: 예측 알고리즘 단위 테스트
- 모든 주요 비즈니스 로직에 대한 테스트 케이스 완료

## 6. 다음 단계 개발 계획

### 🔄 진행 예정 작업
- [ ] 별도 상품 관리 API 개발
- [ ] 상담별 상품 통계 기능 구현
- [ ] 대시보드 기능 추가
- [ ] 실시간 분석 결과 알림 기능
- [ ] 데이터 내보내기 기능

### 🛠️ 추가 개발 요구사항
- [ ] 공통 Exception 처리
  - [ ] 전역 예외 핸들러 구현
  - [ ] 커스텀 예외 클래스 정의
  - [ ] API 에러 응답 표준화
- [ ] Profile별 환경 설정
  - [ ] 개발(dev) 환경 설정
  - [ ] 검증(test) 환경 설정  
  - [ ] 운영(prod) 환경 설정
  - [ ] 환경별 application.yml 분리
- [ ] JWT 기반 인증 시스템
  - [ ] JWT 토큰 생성/검증 로직
  - [ ] 로그인 API 구현
  - [ ] 인증 필터 구현
  - [ ] 사용자 관리 기능

### 🚀 고도화 계획
- [ ] OpenAI API 연동으로 분석 정확도 향상
- [ ] 머신러닝 모델 도입
- [ ] 실시간 스트리밍 분석
- [ ] 다국어 지원

## 7. 브라우저 접속 정보

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:counseldb`
  - User Name: `sa`
  - Password: (빈칸)
