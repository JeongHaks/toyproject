package com.example.toyproject.repository;

import com.example.toyproject.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// DB 테이블 접근용 JPA
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // (post_id, user_id)로 좋아요가 이미 존재하는지 확인
    boolean existsByPostIdAndUserId(Long postId, String userId);

    // 좋아요 취소를 위해 해당 row를 찾을 때 사용
    Optional<PostLike> findByPostIdAndUserId(Long postId, String userId);

    // 게시글의 좋아요 수 계산 (초기에는 count(*)로 가도 충분) : 특정 선택된 게시글의 좋아요 수
    long countByPostId(Long postId);

    // 좋아요 취소(삭제)
    void deleteByPostIdAndUserId(Long postId, String userId);

    /**
     * ✅ Postgres 전용: 중복이면 예외 없이 무시
     * 반환값:
     *  - 1 : insert 성공(새로 좋아요됨)
     *  - 0 : 이미 존재해서 아무것도 안 함(이미 좋아요 상태)
     *  */
    @Modifying
    @Query(value = """
        INSERT INTO post_like (post_id, user_id)
        VALUES (:postId, :userId)
        ON CONFLICT ON CONSTRAINT uk_post_like DO NOTHING
        """, nativeQuery = true)
    int insertIgnoreDuplicate(@Param("postId") Long postId, @Param("userId") String userId);

    long countByPostIdAndUserId(Long postId, String userId);
}
