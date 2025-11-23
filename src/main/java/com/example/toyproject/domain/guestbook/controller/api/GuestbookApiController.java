package com.example.toyproject.domain.guestbook.controller.api;

import com.example.toyproject.domain.guestbook.dto.request.GuestbookCreateRequest;
import com.example.toyproject.domain.guestbook.dto.response.GuestbookResponse;
import com.example.toyproject.domain.guestbook.service.GuestbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invitations/{code}/guestbook")
public class GuestbookApiController {

    private final GuestbookService guestbookService;

    /**
     * 방명록 목록 조회
     */
    @GetMapping
    public List<GuestbookResponse> getGuestbooks(@PathVariable("code") String code) {
        return guestbookService.getGuestbooks(code);
    }

    /**
     * 방명록 작성
     */
    @PostMapping
    public GuestbookResponse addGuestbook(@PathVariable("code") String code,
                                          @RequestBody GuestbookCreateRequest request) {
        return guestbookService.addGuestbook(code, request);
    }
}
