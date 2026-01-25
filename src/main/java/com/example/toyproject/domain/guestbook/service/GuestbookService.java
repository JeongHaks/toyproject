package com.example.toyproject.domain.guestbook.service;

import com.example.toyproject.domain.guestbook.dto.request.GuestbookCreateRequest;
import com.example.toyproject.domain.guestbook.dto.response.GuestbookResponse;
import com.example.toyproject.domain.invitation.entity.Guestbook;
import com.example.toyproject.domain.invitation.entity.Invitation;
import com.example.toyproject.domain.guestbook.repository.GuestbookRepository;
import com.example.toyproject.domain.invitation.repository.InvitationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // 생성자 주입 자동 생성
@Transactional(readOnly = true)
public class GuestbookService {

    private final GuestbookRepository guestbookRepository;
    private final InvitationRepository invitationRepository;

    /**
     * 방명록 조회
     */
    public List<GuestbookResponse> getGuestbooks(String code) {
        System.out.println("모바일 초대장 순서 GuestbookService 1 : " + code);
        List<Guestbook> list = guestbookRepository.findByInvitation_CodeOrderByCreatedAtDesc(code);

        return list.stream()
                .map(GuestbookResponse::new)
                .toList();
    }

    /**
     * 방명록 작성
     */
    @Transactional
    public GuestbookResponse addGuestbook(String code, GuestbookCreateRequest request) {
        System.out.println("모바일 초대장 순서 GuestbookService 2 : " + code);
        System.out.println("모바일 초대장 순서 GuestbookService 2 : " + request);
        // 초대장 찾기
        Invitation invitation = invitationRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("초대장을 찾을 수 없습니다. code=" + code));

        // 방명록 엔티티 생성
        Guestbook guestbook = Guestbook.create(
                invitation,
                request.getGuestName(),
                request.getMessage()
        );

        // 저장 후 응답 DTO로 변환
        Guestbook saved = guestbookRepository.save(guestbook);
        return new GuestbookResponse(saved);
    }

    // 방명록 삭제
    @Transactional
    public void deleteGuestbook(String code, Long guestid){
        System.out.println("모바일 초대장 순서 GuestbookService 3 : " + code);
        System.out.println("모바일 초대장 순서 GuestbookService 3 : " + guestid);
        Guestbook gb = guestbookRepository.findByIdAndInvitation_Code(guestid,code)
                .orElseThrow(()->new IllegalArgumentException("해당 방명록이 존재하지 않습니다."));

        guestbookRepository.delete(gb);
    }
}
