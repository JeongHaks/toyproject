package com.example.toyproject.common;
/*
* 작성자 : 조정학
* 작성일 : 2025-10-18
* */


import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

// 전역 예외 핸들러 클래스
/*
* 전역 예외 처리 클래스이다.
*  - 모든 Controller에서 발생한 예외를 한 곳에서 처리한다.
*  - 예외 종류별로 다른 템플릿(403, 404, 500 등등)으로 연결한다.
*  - 사용자에게는 친절한 오류페이지 화면, 개발자에겐 일관된 처리 흐름을 제공
* */
public class GlobalExceptionHandler {

    /*
    * 존재하지 않는 리조스(게시글 등등) 요청 시 404 페이지로 이동한다.
    * ex) postService.get(id) 호출 시 id가 없을 경우 404 페이지로 이동한다.
    * */
    @ExceptionHandler(ConfigDataResourceNotFoundException.class)
    public String handleNotFound(ConfigDataResourceNotFoundException ex, Model model){
        model.addAttribute("message", ex.getMessage());
        return "404"; //templates/404.html 로 이동
    }

    /*
    * [403] 권한 없는 접근시
    * ex) 작성자가 아닌 사용자가 수정/삭제 요청을 보냈을 때
    * - templates > 403.html
    * */

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/403";
    }

    /**
     * [500] 그 밖의 모든 서버 내부 오류
     * 예) NullPointerException, IllegalArgumentException 등
     * - templates/error/500.html 로 이동한다.
     * - 실무에서는 여기에서 로깅/모니터링(Sentry, CloudWatch 등)을 호출하기도 한다.
     */
    @ExceptionHandler(Exception.class)
    public String handleServerError(Exception ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/500";
    }
}
