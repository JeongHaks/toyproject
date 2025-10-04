package com.example.toyproject.auth.dto;

/*
* 작성자 : 조정학
* 작성일 : 20250929
* */
// 엔티티 --> 레포지토리 --> 서비스

/*
* REST API 전용
* 회원가입 여창 DTO
*  : 클라이언트에서 보내는 JSON 데이터를 담는 곳이다.
* */
public class SignupRequest {
    public String id; //사용자 ID
    public String password; //사용자 비밀번호(평문 -> 해시처리)
    public String nickname; //사용자 닉네임
}
