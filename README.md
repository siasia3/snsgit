# 🌐 Horizon SNS

실시간 채팅과 소셜 로그인이 가능한 SNS 플랫폼

## 📌 프로젝트 소개

친구들과 소통하고, 실시간으로 채팅할 수 있는 SNS 서비스입니다.
OAuth2 소셜 로그인, WebSocket 실시간 채팅, Redis 캐싱 등 
백엔드 기술을 적용한 학습 프로젝트입니다.

### 🔗 Links
- **Demo:** https://horizonsns.com 

## 🛠 기술 스택

### Backend
- **Framework:** Spring Boot 3.3.4
- **ORM:** JPA, QueryDSL
- **Auth:** Spring Security, JWT, OAuth2 (Google, Naver)
- **Real-time:** WebSocket
- **Cache:** Redis
- **Build:** Gradle

### Frontend
- **Template Engine:** Thymeleaf
- **UI:** HTML5, CSS3, JavaScript

### Infrastructure
- **CI/CD:** GitHub Actions
- **Server:** Oracle Cloud Infrastructure (OCI)
- **Web Server:** nginx (리버스 프록시)
- **SSL:** Let's Encrypt
- **Storage:** AWS S3 + CloudFront, OCI Object Storage
- **Database:** MySQL 8.0

---

## 🎯 주요 기능

### 1. 사용자 인증
- 소셜 로그인 (Google, Naver)
- JWT 토큰 기반 인증
- 자동 로그인 (Refresh Token)

### 2. 게시글 관리
- CRUD 기능
- 이미지/동영상 업로드 (AWS S3,OCI Object Storage)
- 태그 기능
- 좋아요 기능
- 커서 기반 페이징 (무한 스크롤)

### 3. 댓글 시스템
- 댓글/대댓글 작성
- 실시간 댓글 수 업데이트

### 4. 친구 관리
- 친구 요청/수락/거절
- 친구 목록 조회

### 5. 실시간 채팅
- 1:1 채팅
- WebSocket 기반 실시간 통신

---

## 💡 기술적 구현 내용

### 성능 최적화
- **N+1 문제 해결:** QueryDSL Fetch Join 활용
- **페이징 최적화:** 커서 기반 페이징으로 대용량 데이터 처리
- **캐싱:** Redis를 활용한 세션 관리 및 응답 속도 개선

### 보안
- **OAuth2 소셜 로그인:** Google, Naver 연동
- **JWT 인증:** Access Token + Refresh Token
- **HTTPS:** Let's Encrypt SSL 인증서 적용
- 
### 실시간 통신
- **WebSocket:** STOMP 프로토콜 활용
- **메시지 브로커:** Spring Messaging

### 인프라 자동화
- **CI/CD:** GitHub Actions 파이프라인
- **무중단 배포:** nginx 리버스 프록시
- **클라우드:** OCI

---

## 🏗 아키텍처
<img width="968" height="658" alt="image" src="https://github.com/user-attachments/assets/1628b0d5-94f6-4d4d-b8cd-ad4c5cac1fcf" />

---

## 📊 ERD

<img width="945" height="567" alt="snserd" src="https://github.com/user-attachments/assets/64810d25-598b-4f88-8d56-9db71389d388" />

---

## 📚 배운 점 & 회고

### 기술적 성장
- QueryDSL을 활용한 동적 쿼리 작성 능력 향상
- OAuth2 인증 플로우에 대한 깊은 이해
- WebSocket을 이용한 실시간 통신 구현 경험
- CI/CD 파이프라인 구축 및 자동화 경험

### 아쉬운 점
- 테스트 코드 커버리지 부족
- 에러 모니터링 시스템 미구축
- 동시성 처리 미흡

### 향후 개선 계획
- [ ] 핵심 비즈니스 로직 단위 테스트 추가
- [ ] Sentry를 통한 에러 모니터링
- [ ] Redis 분산 락을 활용한 동시성 제어
- [ ] Blue-Green 배포로 무중단 배포 구현

## 👤 개발자

**김래홍**
- GitHub: [@siasia3](https://github.com/siasia3)
- Email: coding0434@naver.com


