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
    @NotBlank(message="제목은 비어 있을 수 없습니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message="본문은 비어있을 수 없습니다.")
    @Size(max=5000,message = "본문은 5000자 이하여야 합니다.")
    private String content;
}
