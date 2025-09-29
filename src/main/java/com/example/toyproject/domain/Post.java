package com.example.toyproject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

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

    @Id
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name="user_id", nullable = false, length = 50)
    private String userId;

    // 가입한 날짜 및 시간을 넣으려고 만든 변수
    private LocalDateTime created_at = LocalDateTime.now();
}