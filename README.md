# toyproject

## 1. 게시판 서비스(Board Service)
Spring Boot 기반으로 인증 보안 중심 구, CRUD, 동시성 제어를 구현한 게시판 서비스입니다.
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
- JSESSIONID 쿠키를 통한 세션 식
- CSRF 토큰을 통한 폼 요청 보호
- BCrypt 해시 처리로 비밀번호 암호화

### 동시성 처리(좋아요 기능)
#### 문제 상황
동일 사용자가 동일 게시글에 대해 동시에 여러 번 좋아요 요청을 보낼 경우  
중복 데이터가 저장될 가능성이 존재했습니다.

#### 해결 전략
- DB 레벨에서 `UNIQUE(post_id, user_id)` 제약조건 설정
- PostgreSQL `INSERT ... ON CONFLICT DO NOTHING` 적용
- 애플리케이션 레벨 Lock이 아닌 **DB 무결성 기반 동시성 제어 전략 선택**

#### 검증 방법
- JUnit5 멀티스레드 테스트 작성
- 동일 사용자(userId)가 동일 게시글(postId)에 대해
  100번 동시 요청을 발생시키는 테스트 수행
- 실제 DB에는 1건만 저장됨을 검증

> 동시성 문제를 애플리케이션 로직이 아닌  
> 데이터베이스 제약 조건을 중심으로 해결한 경험입니다.

### 설계 포인트

- 댓글/대댓글은 1 Depth 구조로 제한하여 정렬 복잡도 최소화
- 문자열 정렬 문제 해결을 위해 정렬 기준 재검토
- 트랜잭션 기반 데이터 일관성 유지

### 기술 스택
**Backend**
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security

**Database**
- PostgreSQL

**Testing**
- JUnit 5 (멀티스레드 동시성 테스트)

**View / Build**
- Thymeleaf
- Gradle
