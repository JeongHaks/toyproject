package com.example.toyproject.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

// 동시성 관련 DB Entity
@Entity
@Table(
        name = "post_like",
        uniqueConstraints ={
                // post_id + user_id 조합은 중복될 수 없다 한 명의 유저가 한 게시글에 좋아요를 두 번 누를 수 없게 하기 위해 유니크 선언
                @UniqueConstraint(name="uk_post_like", columnNames = {"post_id", "user_id"})
        }
)
@Getter
@Setter
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    // users.id가 varchar(255)라서 String
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    // JPA(Hibernate)는 리플렉션으로 엔티티 객체를 생성
    // 매개변수 없는 생성자가 반드시 필요
    // 외부에서 함부로 new PostLike() 사용 못 하게 하려고 protected 접근제어자 사용
    protected PostLike() {}

    // 좋아요수 생성을 위해 게시판 ID, 로그인한 사용자 ID 가 필요해서 생성자로 생성함
    // 좋아요는 특정 게시글(postId)과 사용자(userId)가 있어야만 생성될 수 있다
    public PostLike(Long postId, String userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
