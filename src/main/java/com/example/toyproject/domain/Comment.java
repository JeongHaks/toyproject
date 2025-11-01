package com.example.toyproject.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/*
* 작성자 : 조정학
* 작성일 : 20251024
* */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="comment",
        indexes = {
                @Index(name = "idx_post_group_order", columnList = "post_id, group_id, order_in_group")
        })
// DB 테이블과 매핑
public class Comment {
    //DB post_id , user_id , "content" , parent_id , created_at
    @Id
    private String id; // 직접 세팅할 거라 @GeneratedValue 생략 (UUID.randomUUID())

    @Column(name = "post_id", nullable = false)
    private String postId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "parent_id")
    private String parentId; // 최상위면 null

    @Column(name = "group_id")
    private String groupId;  // 루트 댓글의 id (대댓글은 부모의 group_id 상속)

    @Column(name = "order_in_group")
    private Integer orderInGroup; // 같은 group 내 정렬순서 (부모 0, 그 아래 1..N)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();  // ✅ UUID → 문자열 변환
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}
