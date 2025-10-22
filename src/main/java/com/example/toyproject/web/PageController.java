package com.example.toyproject.web;

/*
* 작성자 : 조정학
* 작성일 : 20250929
* */

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // 첫 페이지 화면 localhost:8080/login
    // login.html 이동
    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @GetMapping("/")
    public String home(Model model,
                       @AuthenticationPrincipal UserDetails user) {
        // 로그인 상태면 계정명, 아니면 null
        String username = (user != null) ? user.getUsername() : null;
        model.addAttribute("username", username);
        return "home"; // templates/home.html
    }
}
