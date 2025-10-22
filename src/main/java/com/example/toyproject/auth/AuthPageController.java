package com.example.toyproject.auth;

/*
* 작성자 : 조정학
* 작성일 : 20250929
* SSR 방식 전용
* */


import com.example.toyproject.auth.dto.SignupForm;
import com.example.toyproject.auth.dto.SignupRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SSR 회원가입 컨트롤러
 * - GET /auth/signup → signup.html 화면
 * - POST /auth/signup → 가입 처리 후 /login 리다이렉트
 */
@Controller
@RequestMapping("/auth")
public class AuthPageController {

    private final AuthService authService;

    public AuthPageController(AuthService authService) {
        this.authService = authService;
    }

    // 회원가입 html 이동
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup"; // templates/auth/signup.html
    }

    // 회원가입 정보 DB에 저장하기.
    @PostMapping("/signup")
    public String signupSubmit(@Valid @ModelAttribute("signupForm") SignupForm form,
                               BindingResult bindingResult) {
        // 회원가입 실패시 회원가입 화면으로 이동
        if (bindingResult.hasErrors()) return "signup";

        // SignupForm form html에서 입력한 정보가 담긴 값
        try {
            SignupRequest req = new SignupRequest();
            req.id = form.getId();
            req.password = form.getPassword();
            req.nickname = form.getNickname();
            authService.signup(req);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("signupFail", e.getMessage());
            return "signup";
        }

        // 회원가입 완료 시 로그인 화면으로 이동
        return "redirect:/login?signup=success";
    }
}
