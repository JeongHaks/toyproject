package com.example.toyproject.domain.guestbook.dto.response;


import com.example.toyproject.domain.invitation.entity.Guestbook;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 방명록 응답 DTO
 * - 클라이언트(모바일 청첩장 화면)에 내려줄 데이터 구조
 */
@Getter
public class GuestbookResponse {
    private final Long id;
    private final String guestName;
    private final String message;
    private final LocalDateTime createdAt;

    public GuestbookResponse(Guestbook guestbook) {
        this.id = guestbook.getId();
        this.guestName = guestbook.getGuestName();
        this.message = guestbook.getMessage();
        this.createdAt = guestbook.getCreatedAt();
    }
}
