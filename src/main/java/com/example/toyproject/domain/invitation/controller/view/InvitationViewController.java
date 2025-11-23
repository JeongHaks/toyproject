package com.example.toyproject.domain.invitation.controller.view;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * InvitationViewController
 * ----------------------------------------------------
 * - 관리자(본인)가 사용하는 "화면(HTML)"을 반환하는 컨트롤러
 * - Thymeleaf 같은 템플릿 엔진으로 HTML 페이지를 렌더링할 때 사용
 *
 *  [이번 단계에서 하는 것]
 *   - 초대장 생성 폼 페이지 열기 (GET /admin/invitations/new)
 */
@Controller
@RequestMapping("/invitation")
public class InvitationViewController {

        /**
     * 손님용: 모바일 청첩장 보기 화면
     * ------------------------------------------------
     *  GET /invitation/{code}
     *
     *  예)
     *   - GET /invitation/aZ19lPxQ
     *
     *  - 초대장을 받은 손님이 휴대폰에서 보는 URL
     *  - 여기서는 화면 템플릿만 내려주고
     *    실제 데이터는 화면의 JS가
     *    /api/v1/invitations/{code} 를 호출해서 가져오도록 할 예정
     */
    @GetMapping("/{code}")
    //@ResponseBody
    public String showInvitationView(@PathVariable("code") String code, Model model) {

        // model에 담아서 view로 넘겨준다 코드값
        model.addAttribute("code", code);

        // templates/invitation/view.html 을 가리킴 (다음 단계에서 만들 예정)
        return "invitations/view";
        //return "OK : " + code;
    }
}
