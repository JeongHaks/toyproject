package com.example.toyproject.post;

/*
 * 작성자 : 조정학
 * 작성일 : 20251004
 *
 * SSR 게시판 컨트롤러 (templates 아래에 list.html / detail.html / form.html 배치)
 * - 목록/상세/작성/수정/삭제
 * - 로그인 사용자 ID는 Authentication#getName()
 */

import com.example.toyproject.domain.Comment;
import com.example.toyproject.domain.Post;
import com.example.toyproject.post.dto.PostForm;
import com.example.toyproject.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService)
    {
        this.postService = postService;
        this.commentService =commentService;
    }


    /** 게시판 목록 (최신순, 페이징) */
    // 게시판 이동 누르면 오늘 함수
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       Model model) {
        Page<Post> posts = postService.list(keyword, page, size);
        System.out.printf("DEBUG page=%d, size=%d, totalElements=%d, totalPages=%d, contentSize=%d%n",
                posts.getNumber(), posts.getSize(), posts.getTotalElements(), posts.getTotalPages(), posts.getContent().size());
        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);
        return "list"; // templates/list.html
    }

    /** 상세 */
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model, Authentication auth) {
        // 1. 게시글 단건 조회
        Post post = postService.get(id);

        // 로그인 사용자 확인한 후 내 글 여부 판단한다.
        String currentUser = (auth != null) ? auth.getName() : null;
        boolean mine = currentUser != null && currentUser.equals(post.getUserId());

        // 댓글 목록 조회
        List<Comment> comments = commentService.getCommentsByPost(String.valueOf(id));




        // 모델에 담기.
        model.addAttribute("post", post);
        model.addAttribute("mine", mine); // 내 글이면 수정/삭제 버튼 노출
        model.addAttribute("comments", comments);  // 댓글 목록
        model.addAttribute("commentsCount", comments.size()); // 댓글 수

        return "detail"; // templates/detail.html
    }

    /** 작성 폼 */
    @GetMapping("/new")
    public String newForm(Model model) {
        if(!model.containsAttribute("postForm")){ // 이름 : postForm
            model.addAttribute("postForm", new PostForm()); // 타입 : postForm으로 통일
        }
        return "form"; // templates/form.html
    }

    /** 작성 처리 */
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("postForm") PostForm form,
                         RedirectAttributes ra,
                         BindingResult bindingResult,
                         Authentication auth) {
        // BindingResult는 @ModelAttribute 바로 다음에 위치해야 유효성 에러가 바인딩됨
        // 유효성 검사
        if (bindingResult.hasErrors()) return "form";

        String writerId = auth.getName(); // 로그인 사용자 ID
        Long id = postService.create(form.getTitle(), form.getContent(), writerId);
        ra.addFlashAttribute("msg", "게시글이 등록되었습니다.");
        return "redirect:/posts/" + id; // 작성 후 상세로
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, Authentication auth) {
        // 유효성 검사
        if (!model.containsAttribute("post")) {
            var post = postService.get(id); // Post 반환 (title, content 있음)
            PostForm req = new PostForm();
            req.setTitle(post.getTitle());
            req.setContent(post.getContent());
            model.addAttribute("postForm", req);
        }
        model.addAttribute("postId", id);
        return "form";
    }

    /** 수정 처리 */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("postForm") PostForm form,
                         BindingResult bindingResult,
                         Authentication auth,
                         Model model) {
        // ⚠️ 에러 시에도 postId를 다시 넣어줘야 form의 th:action 분기가 유지됨
        // 유효성 검사
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            return "form";
        }
        postService.update(id, form.getTitle(), form.getContent(), auth.getName());
        return "redirect:/posts/" + id; // 수정 후 상세로
    }

    /** 삭제 (POST) */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id
                       , Authentication auth
                       , RedirectAttributes ra) {
        postService.delete(id, auth.getName());
        // 유효성 검사
        ra.addFlashAttribute("msg","게시글이 삭제되었습니다.");

        return "redirect:/posts"; // 삭제 후 목록으로
    }

    /** 댓글 목록 모델 추가*/

}
