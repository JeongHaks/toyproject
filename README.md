# toyproject

## 1. 게시판 서비스(Board Service)
Spring Boot 기반으로 인증/인가, CRUD, 동시성 제어를 구현한 게시판 서비스입니다.
단순 기능 구현을 넘어 데이터 무결성과 보안 구조를 고려하여 설계하였습니다.

### 주요 기능
- 회원가입 / 로그인 기능
- BCrypt 기반 비밀번호 암호화
- Spring Security 기반 세션 인증
- CSRF 보호 설정
- 게시글 CRUD
- 댓글 / 대댓글(1 Depth) 구조 구현
- 검색 기능 (2글자 이상 자동완성)
- 페이징 처리
- 좋아요 기능 및 동시성 처리

### 인증 및 보안 구조
- 로그인 성공시 서버에 세션 생성
- 여기부터 작성시작 

## 좋아요 동시성 테스트
- 동일한 사용자(userId)가 동일한 게시글(postId)에  
  **동시에 100번 좋아요 요청**
- DB UNIQUE(post_id, user_id) 제약조건과  
  `INSERT ... ON CONFLICT DO NOTHING` 사용
- **JUnit 멀티스레드 테스트로 DB에 1건만 저장됨을 검증**

## 기술 스택
- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- JUnit 5
