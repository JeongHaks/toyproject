package com.example.toyproject.post;

/*
 * 작성자 : 조정학
 * 작성일 : 2025-10-04
 *
 * [서비스 역할 한 줄 요약]
 *  - 컨트롤러는 입/출력과 라우팅만 담당하고,
 *  - 핵심 비즈니스(권한 확인, 존재 여부, 트랜잭션 경계, 로깅)는 Service에서 수행한다.
 */

import com.example.toyproject.common.ResourceNotFoundException; // ← 우리 전역 404 매핑용 커스텀 예외
import com.example.toyproject.domain.Post;
import com.example.toyproject.repository.PostRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

// Spring의 @Transactional 사용 권장 (readOnly, rollbackFor 등 옵션이 풍부함)
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;

    // 생성자 주입: 테스트/DI 친화적
    public PostService(PostRepository postRepository){
        this.postRepository = postRepository;
    }

    /**
     * 게시글 작성
     *
     * @param title    제목 (NOT NULL)
     * @param content  본문 (NOT NULL)
     * @param writerId 작성자 ID (현재 로그인 사용자의 ID)
     * @return 생성된 게시글의 PK
     */
    @Transactional // 쓰기 트랜잭션: INSERT 수행
    public long create(String title, String content, String writerId){
        // 1) 파라미터 1차 검증 (Controller @Valid와 별개로 Service에서도 방어적 검증)
        if (isBlank(title) || isBlank(content) || isBlank(writerId)) {
            throw new IllegalArgumentException("제목/본문/작성자는 필수입니다."); // → GlobalExceptionHandler에서 500.html 처리
        }

        // 2) 전처리: 공백 제거(서버 표준화)
        String normalizedTitle = title.trim();
        String normalizedContent = content.trim();

        // 3) 엔티티 생성 및 저장
        Post p = new Post();
        p.setTitle(normalizedTitle);
        p.setContent(normalizedContent);
        p.setUserId(writerId);

        Post saved = postRepository.save(p);

        // 4) 운영 가시성 확보: 중요한 상태 변화는 정보 로그로 남긴다
        log.info("Post created: id={}, writer={}", saved.getId(), writerId);

        // 5) 생성된 PK 반환 (컨트롤러에서 상세 페이지로 리다이렉트 등에 활용)
        return saved.getId();
    }

    /**
     * 게시글 단건 조회
     * - 없으면 404(ResourceNotFoundException) 발생
     * - Controller에서 try/catch 필요 없음: GlobalExceptionHandler가 404 템플릿으로 안내
     */
    @Transactional(readOnly = true) // 읽기 트랜잭션: 영속성 컨텍스트는 유지하되 변경감지는 비활성화(최적화)
    public Post get(Long id) {
        return postRepository.findById(id)
                // NoSuchElementException 대신 ResourceNotFoundException 사용 → 404 매핑
                .orElseThrow(() -> new ResourceNotFoundException("게시글이 존재하지 않습니다. id=" + id));
    }

    /**
     * 게시글 목록 보기
     * 게시글 목록 조회 (최신순, 페이징)
     * - page: 0부터 시작 (음수 방지)
     * - size: 1~100으로 가드(과도한 페이지 크기 방지)
     * - 정렬: id DESC (작성 최신순)
     */
    // 검색 했을 시 리스트화 목록 보기
    @Transactional(readOnly = true)
    public Page<Post> list(String keyword, int page, int size){
        // 1) 페이징하기 위한 값 전달.
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 100); // 1~100 제한

        // 2) 페이징/정렬 객체 함수
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        // 3) 검색 여부 묻기(검색 내용이 있을경우)
        if (keyword != null && !keyword.isBlank()) {
            return postRepository.search(keyword.trim(), pageable); // 검색용 쿼리 실행
        } else { // 검색 내용이 없을 경우
            return postRepository.findAllByOrderByIdDesc(pageable); // 기본 목록
        }
    }

    /**
     * 게시글 수정 (작성자 본인만)
     *
     * @param id          수정할 글 PK
     * @param title       새 제목
     * @param content     새 본문
     * @param currentUser 현재 로그인 사용자 ID (Controller에서 Authentication.getName())
     */
    @Transactional // 쓰기 트랜잭션: Dirty Checking으로 UPDATE 반영
    public void update(Long id, String title, String content, String currentUser) {
        // 1) 존재 확인 (없으면 404)
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글이 존재하지 않습니다. id=" + id));

        // 2) 권한 확인 (작성자 일치 여부) → 불일치면 403
        if (!post.getUserId().equals(currentUser)) {
            // 이 예외는 Spring Security의 AccessDenied 예외로 403.html로 라우팅됨
            throw new AccessDeniedException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        // 3) 값 검증 (Service 레벨 방어 로직)
        if (isBlank(title) || isBlank(content)) {
            throw new IllegalArgumentException("제목/본문은 비어 있을 수 없습니다.");
        }

        // 4) 상태 변경 (JPA Dirty Checking)
        post.setTitle(title.trim());
        post.setContent(content.trim());
        // updatedAt은 엔티티의 @PreUpdate로 자동 갱신한다고 가정

        // 5) 로깅
        log.info("Post updated: id={}, by={}", id, currentUser);
    }

    /**
     * 게시글 삭제 (작성자 본인만)
     * - 존재하지 않으면 404
     * - 권한 없으면 403
     */
    @Transactional
    public void delete(Long id, String currentUser) {
        // 1) 존재 확인
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글이 존재하지 않습니다. id=" + id));

        // 2) 권한 확인
        if (!post.getUserId().equals(currentUser)) {
            throw new AccessDeniedException("본인이 작성한 글만 삭제할 수 있습니다.");
        }

        // 3) 삭제
        postRepository.delete(post);

        // 4) 로깅
        log.info("Post deleted: id={}, by={}", id, currentUser);
    }

    // --- 내부 유틸 ---
    /**
     * 내부 유틸리티: 문자열이 null이거나 공백만으로 구성되었는지 검사
     * - Controller, Service 어디서든 간단 검증에 사용 가능
     * - 공통 유틸로 뺄 수도 있으나, 여기서는 서비스 한정 간단 메서드로 둔다.
     */
    private boolean isBlank(String s){
        return s == null || s.trim().isEmpty();
    }
}
