package com.example.toyproject.postlike;


import com.example.toyproject.domain.PostLike;
import com.example.toyproject.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 좋아요 등록, 취소 기능 생성
// 동시 요청에서도 DB 유니크 제약으로 “중복 좋아요”를 안전하게 처리한다.
@Service
@RequiredArgsConstructor
public class PostLikeService {
    /*
    * 1. 동시성에서 제일 위험한 건 "동시에 두 번 저장"
    * 2. DB DDL에서 이미 UNIQUE(post_id, user_id) 했음
    *   : 동시에 요청이 와도 하나만 등록해줌.
    * 3. 서비스 클래스에서 실패예외 처리해줌(이미 좋아요했습니다.)
    * */
    // 트랜잭션 - 예외 발생시 롤백 하기 위함, 부분 저장 및 꼬임 방지

    // 생성자 선언
    private final PostLikeRepository postLikeRepository;
    /**
     * 좋아요 등록
     *
     * @return true  : 이번 요청으로 좋아요가 새로 등록됨
     *         false : 이미 좋아요가 존재(중복 요청 or 이미 눌러둠)
     */
    @Transactional
    public boolean like(Long postId, String userId) {
        int affected = postLikeRepository.insertIgnoreDuplicate(postId, userId);
        return affected == 1;
    }

    // 좋아요 취소
    @Transactional
    public void unlike(Long postId, String userId) {
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    // 좋아요 개수
    @Transactional(readOnly = true)
    public long countByPostId(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    // 좋아요 현재 로그인한 사용자의 좋아요 상태 조회
    @Transactional(readOnly = true)
    public boolean isLiked(Long postId, String userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

}
