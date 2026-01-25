# toyproject

Spring Boot 기반 게시판 토이 프로젝트입니다.  
학습 및 실무 감각 유지를 목적으로 개발했습니다.

## 주요 기능
- 게시글 CRUD
- 댓글 / 대댓글
- 좋아요 기능
- 좋아요 동시성 처리

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
