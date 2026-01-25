package com.example.toyproject.postlike;

import com.example.toyproject.postlike.dto.PostLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
class PostLikeController {
    /*
    * 1. 좋아요 요청 받기(사용자ID, 게시글ID 등)
    * 2. 서비스 호출 역할
    * 3. 응답 DTO(데이터 전달)에 서비스 결과 데이터 저장
    * */
    private final PostLikeService postLikeService;

    /**
     * 좋아요 등록
     *
     * POST /posts/{postId}/like
     *
     * @return
     *  - liked     : 이번 요청으로 좋아요가 새로 등록되었는지 여부
     *  - likeCount : 현재 게시글의 전체 좋아요 수
     */
    @PostMapping("/{postId}/like")
    public PostLikeResponse like(
            @PathVariable("postId")  Long postId,
            Authentication authentication) {
        // 로그인한 사용자 식별자 (users.id)
        String userId = authentication.getName();

        // 좋아요 시도
        boolean liked = postLikeService.like(postId, userId);

        // 현재 좋아요 개수 조회
        long likeCount = postLikeService.countByPostId(postId);

        return new PostLikeResponse(liked, likeCount);
    }

    /**
     * 좋아요 취소
     *
     * DELETE /posts/{postId}/like
     *
     * @return
     *  - liked     : 현재 좋아요 상태(false)
     *  - likeCount : 현재 게시글의 전체 좋아요 수
     */
    @DeleteMapping("/{postId}/like")
    public PostLikeResponse unlike(
            @PathVariable("postId")  Long postId,
            Authentication authentication
    ) {
        String userId = authentication.getName();

        // 좋아요 취소 시도 (없으면 아무 일도 안 일어남)
        postLikeService.unlike(postId, userId);

        long likeCount = postLikeService.countByPostId(postId);

        // 취소 후 상태는 항상 "좋아요 안 함"
        return new PostLikeResponse(false, likeCount);
    }



}
