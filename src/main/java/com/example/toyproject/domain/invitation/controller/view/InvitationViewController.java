package com.example.toyproject.domain.invitation.controller.view;


import com.example.toyproject.domain.invitation.entity.Invitation;
import com.example.toyproject.domain.invitation.service.InvitationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/invitation")
public class InvitationViewController {

    private final InvitationService invitationService;

    /**
     * 손님용: 모바일 청첩장 보기 화면
     * ------------------------------------------------
     * GET /invitation/{code}
     * <p>
     * 예)
     * - GET /invitation/aZ19lPxQ
     * <p>
     * - 초대장을 받은 손님이 휴대폰에서 보는 URL
     * - 여기서는 화면 템플릿만 내려주고
     * 실제 데이터는 화면의 JS가
     * /api/v1/invitations/{code} 를 호출해서 가져오도록 할 예정
     */
    @GetMapping("/{code}")
    //@ResponseBody
    public String showInvitationView(@PathVariable("code") String code
            , Model model
            , HttpServletRequest request) {

        // 초대장 Entity 조회
        Invitation invitation = invitationService.getInvitationEntityByCode(code);
        // model에 담아서 view로 넘겨준다 코드값
        model.addAttribute("invitation", invitation);
        model.addAttribute("code", code);

        // 외부 접근 가능한 절대 URL 경로(썸네일용)
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort());

        if (invitation.getMainImageUrl() != null && invitation.getMainImageUrl().isBlank()) {
            if(invitation.getMainImageUrl().startsWith("http://") || invitation.getMainImageUrl().startsWith("https://")){
                model.addAttribute("ogImageUrl",invitation.getMainImageUrl());
            }else{
                if(!invitation.getMainImageUrl().startsWith("/")){
                    String mainImageUrl = "/" + invitation.getMainImageUrl();
                    model.addAttribute("ogImageUrl",baseUrl + mainImageUrl);
                }
            }
        }else{
            model.addAttribute("ogImageUrl", baseUrl + invitation.getMainImageUrl());
        }

        // templates/invitation/view.html 을 가리킴 (다음 단계에서 만들 예정)
        return "invitations/view";
        //return "OK : " + code;
    }
}


