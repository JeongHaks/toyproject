package com.example.toyproject.domain.guestbook.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 방명록 작성 요청 DTO
 * - 손님이 이름 + 메시지 입력해서 방명록을 남길 때 사용
 */
@Getter
@NoArgsConstructor
public class GuestbookCreateRequest {

    private String guestName; //작성자(손님) 이름
    private String message; //축하 메시지
}
