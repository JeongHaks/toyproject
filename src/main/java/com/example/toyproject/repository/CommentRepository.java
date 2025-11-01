package com.example.toyproject.repository;

/*
* 작성자 : 조정학
* 작성일 : 20251024
* */

import com.example.toyproject.domain.Comment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/*
* 댓글 엔티티와 DB 테이블을 연결하는 JPA 인터페이스 공간
* 기본 CRUD 기능(save, findById, delete 등)은 JpaRepository에서 상속받아서 사용가능
* 커스텀 조회 메서드 : 게시글(postid) 기준으로 댓글 목록 조회
* String로 한 이유는 댓글 테이블의 기본키(id) 생성 방식을 일관성 있게 하려는 설계 선택
* */
public interface CommentRepository extends JpaRepository<Comment, String> {
    // 정렬 조회 (백필 전 NULL 안전 정렬 포함)
    @Query("""
             select c
             from Comment c
             where c.postId = :postId
             order by
               c.groupId asc,
               c.orderInGroup asc,
               c.createdAt asc
    """)
    List<Comment> findAllForPostOrdered(@Param("postId") String postId);

    // 부모 레코드 잠금 (동시성 안전한 shift-insert)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comment c where c.id = :id")
    Optional<Comment> findByIdForUpdate(@Param("id") String id);

    // 부모 바로 아래 끼워넣기 위해 뒤쪽 순서들 +1로 밀기(20251101)
    @Modifying
    @Query("""
        update Comment c
        set c.orderInGroup = c.orderInGroup + 1
        where c.postId = :postId
        and c.groupId = :groupId
        and c.orderInGroup > :parentOrder
    """)
    int shiftOrders(@Param("postId") String postId
                  , @Param("groupId") String groupId
                  , @Param("parentOrder") int parentOrder);

    // 삭제 정책/표시용: 자식(대댓글) 개수
    long countByParentId(String parentId);
}
