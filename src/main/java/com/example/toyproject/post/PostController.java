package com.example.toyproject.post;

/*
 * 작성자 : 조정학
 * 작성일 : 20251004
 *
 * SSR 게시판 컨트롤러 (templates 아래에 list.html / detail.html / form.html 배치)
 * - 목록/상세/작성/수정/삭제
 * - 로그인 사용자 ID는 Authentication#getName()
 */

import com.example.toyproject.domain.Post;
import com.example.toyproject.post.dto.PostForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** 목록 (최신순, 페이징) */
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       Model model) {
        Page<Post> posts = postService.list(page, size);
        model.addAttribute("posts", posts);
        return "list"; // templates/list.html
    }

    /** 상세 */
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model, Authentication auth) {
        Post post = postService.get(id);
        String currentUser = (auth != null) ? auth.getName() : null;
        boolean mine = currentUser != null && currentUser.equals(post.getUserId());

        model.addAttribute("post", post);
        model.addAttribute("mine", mine); // 내 글이면 수정/삭제 버튼 노출
        return "detail"; // templates/detail.html
    }

    /** 작성 폼 */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm()); // th:object 바인딩용
        return "form"; // templates/form.html
    }

    /** 작성 처리 */
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("postForm") PostForm form,
                         BindingResult bindingResult,
                         Authentication auth) {
        // BindingResult는 @ModelAttribute 바로 다음에 위치해야 유효성 에러가 바인딩됨
        if (bindingResult.hasErrors()) return "form";

        String writerId = auth.getName(); // 로그인 사용자 ID
        Long id = postService.create(form.getTitle(), form.getContent(), writerId);
        return "redirect:/posts/" + id; // 작성 후 상세로
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, Authentication auth) {
        Post post = postService.get(id);
        // 서버 사이드에서도 2차 권한 체크(뷰에서 버튼 숨김과 별개)
        if (auth == null || !post.getUserId().equals(auth.getName())) {
            return "redirect:/posts/" + id;
        }

        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setContent(post.getContent());

        model.addAttribute("postForm", form);
        model.addAttribute("postId", id); // 폼 action 분기용
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
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            return "form";
        }
        postService.update(id, form.getTitle(), form.getContent(), auth.getName());
        return "redirect:/posts/" + id; // 수정 후 상세로
    }

    /** 삭제 (POST) */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth) {
        postService.delete(id, auth.getName());
        return "redirect:/posts"; // 삭제 후 목록으로
    }
}
