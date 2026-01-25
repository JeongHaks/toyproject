package com.example.toyproject.domain.invitation.controller.view;

import com.example.toyproject.domain.guestbook.repository.GuestbookRepository;
import com.example.toyproject.domain.guestbook.service.GuestbookService;
import com.example.toyproject.domain.invitation.entity.Invitation;
import com.example.toyproject.domain.invitation.service.InvitationService;
import com.example.toyproject.domain.invitationimage.service.InvitationImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
@RequestMapping("/admin/invitations")
public class AdminInvitationController {

    private final GuestbookRepository guestbookRepository;
    private final GuestbookService guestbookService;
    private final InvitationService invitationService;
    private final InvitationImageService imageService;

    // 모바일 청첩장 관리
    @GetMapping
    public String invitationAdminHome(Model model){
        System.out.println("모바일 초대장 순서 AdminInvitationController 1");

        List<Invitation> invitatins = invitationService.findAll();

        model.addAttribute("invitations",invitatins);
        return "invitations/admin/invitation-admin-home";
    }

    /**
     * 초대장 생성 폼 화면
     * ------------------------------------------------
     * [HTTP]
     * GET /admin/invitations/new
     * <p>
     * - 관리자(본인)가 브라우저에서 접속하는 URL
     * - 아직은 단순히 "invitation/new" 라는 뷰 이름만 반환
     * - 나중에 templates/invitation/new.html 파일을 만들어서 이 뷰를 구현할 예정
     */
    // 초대장 생성 
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        System.out.println("모바일 초대장 순서 AdminInvitationController 2");
        // 화면 단에 기본 값, 폼 객체 등을 넘기고 싶으면 여기서 model.addAttribute() 사용
        // 예) model.addAttribute("form", new InvitationCreateForm());

        // resources/templates/invitation/new.html 을 가리키는 뷰 이름
        return "invitations/new";
    }

    // 방명록 삭제
    @DeleteMapping("/{code}/guestbook/{guestid}")
    public ResponseEntity<Void> deleteGuestbook(@PathVariable("code") String code
                                               ,@PathVariable("guestid") Long guestid){
        System.out.println("모바일 초대장 순서 AdminInvitationController 3 : " + code);
        System.out.println("모바일 초대장 순서 AdminInvitationController 3 : " + guestid);
        guestbookService.deleteGuestbook(code,guestid);
        return ResponseEntity.noContent().build();
    }

    // 관리용
    @GetMapping("/{code}/guestbook")
    public String showGuestbookAdminPage(@PathVariable("code") String code, Model model){
        System.out.println("모바일 초대장 순서 AdminInvitationController 5 : " + code);
        model.addAttribute("code",code);
        return "invitations/admin/invitation-guestbook";
    }

    /**
     * 갤러리 관리 화면 이동
     */
    @GetMapping("/{code}/images-page")
    public String galleryAdmin(@PathVariable("code") String code, Model model) {
        System.out.println("모바일 초대장 순서 AdminInvitationController 6 : " + code);
        model.addAttribute("code", code);
        return "invitations/admin/invitation-images"; // templates/admin/invitation-images.html
    }

    // 메인 이미지
    @PutMapping("/{code}/main-image")
    @ResponseBody
    public ResponseEntity<Void> updateMainImage(@PathVariable("code") String code,
                                                @RequestParam("imageUrl") String imageUrl) {
        System.out.println("모바일 초대장 순서 AdminInvitationController 7 : " + code);
        System.out.println("모바일 초대장 순서 AdminInvitationController 7 : " + imageUrl);
        invitationService.updateMainImageUrl(code, imageUrl);
        // 바디 없이 204 No Content
        return ResponseEntity.noContent().build();
    }


}
