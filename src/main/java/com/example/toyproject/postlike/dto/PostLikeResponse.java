package com.example.toyproject.postlike.dto;

// 좋아요 취소, 등록 응답 데이터 (갯수포함)
public record PostLikeResponse (
     /* - liked     : 현재 로그인 사용자의 좋아요 상태
        - likeCount : 게시글의 전체 좋아요 수
      */
    boolean liked, //  좋아요 상태 확인
    long likeCount // 전체 좋아요 개수
){}