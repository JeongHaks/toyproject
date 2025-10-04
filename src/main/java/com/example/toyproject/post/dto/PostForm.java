package com.example.toyproject.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/*
* 작성자 : 조정학
* 작성일 : 20251004
* */

/*
 * 게시글 작성/수정 폼과 바인딩되는 DTO
 * - 화면 검증용(SSR)
 */
@Getter
@Setter
public class PostForm {
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;
}
