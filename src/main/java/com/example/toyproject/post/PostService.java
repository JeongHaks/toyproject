package com.example.toyproject.post;

/*
* 작성자 : 조정학
* 작성일 : 20251004
* */

import com.example.toyproject.domain.Post;
import com.example.toyproject.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * 게시판 비즈니스 로직(Service) (질문3) 비즈니스 로직이란?
 *  - 컨트롤러는 "입출력/이동"만 담당하고, 핵심 로직은 여기서 처리
 *  - 트랜잭션/권한/검증/로깅 등을 한 곳에 모아두면 유지보수 쉬움
 */
@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;

    // 생성자 의존성 주입
    public PostService(PostRepository postRepository){
        this.postRepository = postRepository;
    }
    /*
     * 게시글 작성
     * @param title    제목 (NOT NULL)
     * @param content  본문 (NOT NULL)
     * @param writerId 작성자 ID (현재 로그인 사용자의 ID)
     * @return 생성된 게시글의 PK
     */
    @Transactional // (질문4) 어노테이션 설명
    public long create(String title, String content, String writerId){
        // 간단 검증
        if (isBlank(title) || isBlank(content) || isBlank(writerId)) {
            throw new IllegalArgumentException("제목/본문/작성자는 필수입니다.");
        }

        Post p = new Post();
        p.setTitle(title);
        p.setContent(content);
        p.setUserId(writerId);

        Post saved = postRepository.save(p);
        log.info("Post created: id={}, writer={}", saved.getId(), writerId);


        return saved.getId(); // (질문) getId()를 가져오는 이유
    }

    /*
     * 작성한 게시글이 없을 때
     * @throws NoSuchElementException 게시글 없을 때
     */
    // 작성자가 글을 작성했는지 안 했는지 확인
    @Transactional
    public Post get(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다. id=" + id));
    }

    /**
     * 목록 조회 (최신순, 페이징)
     */
    //(질문) 소스 설명 필요
    @Transactional
    public Page<Post> list(int page, int size){
        // 페이지 / 사이즈
        int p = Math.max(page,0);
        int s = Math.min(Math.max(size,1),100);
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        return postRepository.findAll(pageable);
    }

    /*
     * 수정: 작성자 본인만 가능
     * @param id          수정할 글 PK
     * @param title       새 제목
     * @param content     새 본문
     * @param currentUser 현재 로그인 사용자 ID
     */
    @Transactional
    public void update(Long id, String title, String content, String currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다. id=" + id));

        // 권한 체크: 작성자만 수정 가능
        if (!post.getUserId().equals(currentUser)) {
            throw new AccessDeniedException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        if (isBlank(title) || isBlank(content)) {
            throw new IllegalArgumentException("제목/본문은 비어 있을 수 없습니다.");
        }

        post.setTitle(title.trim());
        post.setContent(content);
        // updatedAt은 @PreUpdate로 자동 갱신 (엔티티에 구현해 둠)
        log.info("Post updated: id={}, by={}", id, currentUser);
    }

    /**
     * 삭제: 작성자 본인만 가능
     */
    @Transactional
    public void delete(Long id, String currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다. id=" + id));

        if (!post.getUserId().equals(currentUser)) {
            throw new AccessDeniedException("본인이 작성한 글만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
        log.info("Post deleted: id={}, by={}", id, currentUser);
    }

    // --- 내부 유틸 --- (질문) 내부 유틸리티가 뭣이냐?
    private boolean isBlank(String s){
        return s == null || s.trim().isEmpty();
    }

}
