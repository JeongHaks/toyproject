package com.example.toyproject.rag.chunk;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RagPageController {

    // 메인에서 생성형 AI 버튼 클릭시 페이지 이동
    @GetMapping("/ai")
    public String aiPage(){
        System.out.println("화면페이지 이동 (1)");
        return "rag/rag-test";
    }
}
