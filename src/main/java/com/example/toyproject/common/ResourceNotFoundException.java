package com.example.toyproject.common;

/*
* 요청한 자원(게시글, 댓글 등)을 찾을 수 없을 때 사용되는 사용자 정의 예외
*  - GlobalExceptionHandler 가 이 예외를 받아 404 페이지로 이동시킨다.
*  - 서비스 레이어에서 .orElseThrow(...) 로 자주 사용한다.
* */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message); // 메시지는 오류 화면에서 사용자에게 그대로 노출된다.
    }
}
