package com.example.toyproject.web;

/*
* 작성자 : 조정학
* 작성일 : 20251024
* */

import com.example.toyproject.domain.Comment;
import com.example.toyproject.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/*
 * 이번 단계 -- "댓글 등록"만 담당 한다(SSR : 폼 submit --> redirect)
 * - 목록 조회는 다음 단계에서 Post 상세 화면 구성 시 PostController에서 처리
 *
 * 사용 예)
 *  POST /comment/add
 *   - form fields: postid, userid, content, (parentid: 선택)
 *   - 처리 후: /post/{postid} 로 redirect
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 등록
     *
     * @param postid   게시글 ID (필수)
     * @param userid   작성자 ID (필수)  *실무에선 SecurityContext에서 가져오는 방식 권장 -> 추후 개선
     * @param content  댓글 내용 (필수)
     * @param parentid 부모 댓글 ID (선택: 대댓글이 아닐 경우 null/빈문자 가능)
     * @return 등록 후 해당 게시글 상세 화면으로 리다이렉트
     */
    @PostMapping("/add")
    public String addComment(@RequestParam("postid") String postid,
                             @RequestParam("content") String content,
                             @RequestParam(value="parentid",required = false) String parentid,
                             Authentication auth) {
        String cleanParentId = (parentid == null || parentid.isBlank() || "null".equalsIgnoreCase(parentid))
                ? null : parentid;
        // 클라이언트가 보내는 userid는 신뢰하지 않고, 서버에서 추출한다.
        String userid = (auth != null) ? auth.getName() : null;
        if(userid == null || userid.isBlank()){
            // 미인증 사용자는 로그인 페이지로 유도하거나 에러 표시
            return "redirect:/login";
        }

        // 서비스 계층에 위임 (유효성 검증 포함)
        //var newId = commentService.addRootComment(postid, userid, content);

        Comment saved = (cleanParentId == null)
                ? commentService.addRootComment(postid, userid, content)
                : commentService.addChildComment(postid, cleanParentId, userid, content);

        // 등록 후 해당 게시글 상세 페이지로 이동
        return "redirect:/posts/" + postid + "#c-" + saved.getId();
    }

    /**
     * 댓글 수정 (작성자만)
     * - form에서 commentId, postid, content 를 전달받음
     * - Authentication으로 로그인 사용자 ID를 주입받아 권한 체크
     */
    @PostMapping("/update")
    public String updateComment(@RequestParam("commentId") String commentId,
                                @RequestParam("postid") String postid,
                                @RequestParam("content") String newContent,
                                Authentication auth,
                                RedirectAttributes ra) {
        String requester = (auth != null) ? auth.getName() : null;
        if (requester == null || requester.isBlank()) {
            ra.addFlashAttribute("err", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            commentService.updateComment(commentId, requester, newContent);
            ra.addFlashAttribute("msg", "댓글이 수정되었습니다.");
        } catch (SecurityException se) {
            ra.addFlashAttribute("err", "본인 댓글만 수정할 수 있습니다.");
        } catch (IllegalArgumentException iae) {
            ra.addFlashAttribute("err", iae.getMessage()); // "존재하지 않는 댓글" / "내용을 입력하세요." 등
        } catch (Exception e) {
            ra.addFlashAttribute("err", "알 수 없는 오류가 발생했습니다.");
        }
        return "redirect:/posts/" + postid;
    }

    /**
     * 댓글 삭제 (작성자만)
     * - form에서 commentId, postid 를 전달받음
     * - 자기참조 FK(parent_id) 때문에, 자식(대댓글)이 있으면 DB 제약으로 실패할 수 있음
     */
    @PostMapping("/delete")
    public String deleteComment(@RequestParam("commentId") String commentId,
                                @RequestParam("postid") String postid,
                                Authentication auth,
                                RedirectAttributes ra) {
        String requester = (auth != null) ? auth.getName() : null;
        if (requester == null || requester.isBlank()) {
            ra.addFlashAttribute("err", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            commentService.deleteComment(commentId, requester);
            ra.addFlashAttribute("msg", "댓글이 삭제되었습니다.");
        } catch (IllegalStateException ise) {
            // 자식(대댓글) 존재로 인한 삭제 금지
            ra.addFlashAttribute("err", ise.getMessage()); // "대댓글이 있어 삭제할 수 없습니다."
        } catch (SecurityException se) {
            ra.addFlashAttribute("err", "본인 댓글만 삭제할 수 있습니다.");
        } catch (IllegalArgumentException iae) {
            ra.addFlashAttribute("err", iae.getMessage()); // "존재하지 않는 댓글입니다." 등
        } catch (Exception e) {
            ra.addFlashAttribute("err", "알 수 없는 오류가 발생했습니다.");
        }
        return "redirect:/posts/" + postid;
    }
}