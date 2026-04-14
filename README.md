# toyproject

## 1. 게시판 서비스(Board Service)
Spring Boot 기반으로 인증 보안 중심, CRUD, 동시성 제어를 구현한 게시판 서비스입니다.
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
- JSESSIONID 쿠키를 통한 세션 식별
- CSRF 토큰을 통한 폼 요청 보호
- BCrypt 해시 처리로 비밀번호 암호화

### 동시성 처리(좋아요 기능)
#### 문제 상황
동일 사용자가 동일 게시글에 대해 동시에 여러 번 좋아요 요청을 보낼 경우  
중복 데이터가 저장될 가능성이 존재했습니다.

#### 해결 전략
- DB 레벨에서 `UNIQUE(post_id, user_id)` 제약조건 설정(게시글,사용자)
- PostgreSQL `INSERT ... ON CONFLICT DO NOTHING` 적용
- 애플리케이션 레벨 Lock이 아닌 DB 무결성 기반 동시성 제어 전략 선택

#### 검증 방법
- JUnit5 멀티스레드 테스트 작성
- 동일 사용자(userId)가 동일 게시글(postId)에 대해
  100번 동시 요청을 발생시키는 테스트 수행
- 실제 DB에는 1건만 저장됨을 검증

> 동시성 문제를 애플리케이션 로직이 아닌 데이터베이스 제약 조건을 중심으로 해결한 경험을 작성하였습니다.

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


## 2. 모바일 청첩장 생성 서비스 (Mobile Wedding Invitation Service)
사용자가 모바일 청첩장을 생성하고 관리할 수 있도록 REST API 기반으로 설계 및 구현한 서비스입니다.  
실제 배포 환경에서 동작하도록 구성하여, 로컬 개발을 넘어 운영 환경 이슈까지 경험한 프로젝트입니다.

### 주요 기능
- 청첩장 생성 / 수정 / 삭제 (REST API 설계)
- 사용자 입력 데이터 기반 동적 화면 렌더링
- 이미지 업로드 및 외부 접근 처리
- 배포 환경 구성 및 외부 접속 테스트

### 도메인 설계
- JPA 기반 Entity 설계
- 연관 관계 매핑을 통한 도메인 모델 구성
- 사용자(User) - 청첩장(Invitation) 구조 설계
- 생성/수정/조회 흐름에 맞춘 계층 분리 (Controller / Service / Repository)
> 단순 CRUD가 아닌, 실제 서비스 흐름을 고려하여 도메인 중심으로 설계하였습니다.

### 이미지 업로드 구조
#### 문제 상황
로컬 환경에서는 이미지가 정상 표시되었으나,  
배포 환경에서는 서버 내부 저장 경로 문제로 인해 이미지가 노출되지 않는 이슈 발생

#### 해결 전략
- 로컬 파일 저장 방식 → AWS S3 저장 구조로 전환
- 외부 접근 가능한 URL 기반 조회 구조로 변경
- 서버 환경에 종속되지 않는 저장 방식으로 개선
> 저장소 분리 전략을 통해 배포 환경에서도 안정적으로 이미지 제공 가능하도록 개선하였습니다.

### 배포 및 환경 분리
- Render 무료 호스팅 환경에 배포
- GitHub 연동 자동 배포 구성
- application-local / application-prod 프로파일 분리
- 환경 변수 기반 DB 설정 적용
> 로컬과 운영 환경을 분리하여 설정 충돌 문제를 해결하고, 실제 서비스 환경에서 동작하는 구조를 경험하였습니다.

### 기술 스택
**Backend**
- Java 17
- Spring Boot
- Spring Data JPA

**Database**
- PostgreSQL

**Cloud / Storage**
- AWS S3

**Deployment**
- Render (GitHub 연동 자동 배포)

**View**
- Thymeleaf

**Build / Tool**
- Gradle
- GitHub


## 3. RAG 기반 문서 질의응답 시스템 (RAG Q&A Service)
PDF 문서를 업로드하면 문서 내용을 기반으로 답변을 생성하는  
RAG(Retrieval-Augmented Generation) 구조의 질의응답 시스템입니다.

단순히 LLM을 호출하는 것이 아니라,  
문서 임베딩 → 벡터 저장 → 유사도 검색 → 프롬프트 구성 → 답변 생성까지  
전체 파이프라인을 직접 설계하고 구현하였습니다.

### 시스템 아키텍처 흐름
1. PDF 업로드
2. 텍스트 추출
3. 800자 기준 Chunk 분할 + 150자 Overlap 적용
4. Ollama 기반 임베딩 생성
5. PostgreSQL(pgvector)에 벡터 저장
6. 사용자 질문 입력
7. 벡터 유사도 연산 기반 Top-K 검색
8. 검색된 문맥을 포함하여 LLM 프롬프트 구성
9. 근거 기반 답변 생성

### Chunk 전략
- Chunk Size: 800자
- Overlap: 150자

#### 설계 의도
- 문맥 유지와 토큰 비용의 균형 고려
- Chunk 경계에서 문장 단절 방지
- 검색 정확도 향상

### 벡터 검색 설계
- pgvector 확장 사용
- 코사인 유사도 기반 Top-K 검색
- 질문과 가장 유사한 문맥만 LLM에 전달
> 전체 문서를 그대로 전달하지 않고,  
> 검색된 문맥만 전달하여 응답 정확도 및 효율성 개선

### hallucination 제어(LLM이 근거 없이 답하지 못하게 통제하기 위한 제어)

#### 문제 상황
LLM이 문서와 무관한 내용을 그럴듯하게 생성하는 현상 발생

#### 해결 전략
- 프롬프트에 "문서 기반으로만 답변" 규칙 명시
- 문서에 근거가 없을 경우  
  → "문서에서 찾을 수 없습니다." 출력하도록 정책 설정
- weak context 판단 기준 적용
> LLM의 생성 특성을 이해하고 제어 로직을 설계한 경험

### 트러블슈팅
#### 1.pgvector 오류
- `type "vector" does not exist` 오류 발생
- pgvector 확장 설치 및 DDL 수정으로 해결

#### 2.임베딩 구조 전환
- 외부 임베딩 API 비용/토큰 제약 문제 발생
- 로컬 Ollama 기반 임베딩 구조로 전환
- 개발 환경에서 재현 가능한 구조로 개선

### 기술 스택
**Backend**
- Java 17
- Spring Boot
- Spring Data JPA

**Database**
- PostgreSQL
- pgvector

**AI / LLM**
- Ollama (Local)(경량)

**View**
- Thymeleaf

**Build / Tool**
- Gradle
- GitHub
