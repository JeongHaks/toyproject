package com.example.toyproject.auth;

/*
* 작성자 : 조정학
* 작성일 : 20250929
* SSR 방식 전용
* */


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

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup"; // templates/auth/signup.html
    }

    @PostMapping("/signup")
    public String signupSubmit(@Valid @ModelAttribute("signupForm") SignupForm form,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "signup";

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

        return "redirect:/login?signup=success";
    }
}
