package com.example.toyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
/**
 * 게시글 엔티티 (SSR + 실무형 구조)
 * - PK: Long (자동 증가) → 정렬/조회 효율
 * - 작성자: User.id를 FK로 저장 (User 엔티티 연관관계는 나중에 확장 가능)
 * - 생성/수정 시각: 기본값 LocalDateTime.now() + @PreUpdate
 */


/*
 * 작성자 : 조정학
 * 작성일 : 20250929
 *
 * */

@Entity
@Table(name="post")
@Getter
@Setter
public class Post {

    /** 글 ID (자동 증가 PK) */
    @Id //(질문1) ID라는게 글 넘버인거 ??
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 제목 (200자 제한, NOT NULL) */
    @Column(nullable = false, length = 200)
    private String title;

    /** 본문 (TEXT 컬럼 매핑, NOT NULL) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 작성자 ID (User.id 참조, 문자열 저장) */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    /** 생성 시각 */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 수정 시각 */
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** 엔티티가 업데이트 될 때 자동으로 updatedAt 갱신 */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}