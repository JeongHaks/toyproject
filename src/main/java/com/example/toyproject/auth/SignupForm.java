package com.example.toyproject.auth;

/*
 * 작성자 : 조정학
 * 작성일 : 20250929
 * SSR 방식 전용
 * */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/*
* 회원가입 폼 DTO (SSR 전용)
*  : Thymleaf form과 바인딩한다
*  : 검증 어노테이션 사용
* */
@Getter
@Setter
public class SignupForm {

    @NotBlank
    @Size(min=3, max=50)
    private String id;

    @NotBlank @Size(min = 4, max = 100)
    private String password;

    @NotBlank @Size(max = 50)
    private String nickname;
}
