package com.example.toyproject.domain.invitation.controller.api;


import com.example.toyproject.domain.invitation.dto.request.InvitationCreateRequest;
import com.example.toyproject.domain.invitation.dto.response.InvitationResponse;
import com.example.toyproject.domain.invitation.entity.Invitation;
import com.example.toyproject.domain.invitation.service.InvitationService;
import com.example.toyproject.domain.invitationimage.entity.InvitationImage;
import com.example.toyproject.domain.invitationimage.service.InvitationImageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * InvitationApiController
 * ----------------------------------------------------
 * - 초대장(Invitation) 관련 REST API를 제공하는 컨트롤러
 * - JSON 형식으로 요청/응답을 처리
 *
 *  [주요 기능 - V1 기준]
 *   1) 초대장 생성 (POST /api/v1/invitations)
 *   2) 초대장 조회 (GET /api/v1/invitations/{code})  ← 이건 다음 단계에서 추가 예정
 */
@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationApiController {

    private final InvitationService invitationService;
    private final InvitationImageService invitationImageService;

    /*
    * 초대장 생성 API
    [HTTP]
     *   POST /api/v1/invitations
     *
     * [요청 예시 JSON]
     * {
     *   "title": "정학 ♥ 아무개 결혼식에 초대합니다",
     *   "groomName": "조정학",
     *   "brideName": "김아무개",
     *   "weddingDate": "2026-05-10",
     *   "weddingTime": "13:00",
     *   "hallName": "OO웨딩컨벤션 3층 그랜드홀",
     *   "address": "서울시 OO구 OO로 123",
     *   "mapUrl": "https://map.kakao.com/...",
     *   "mainImageUrl": "https://.../main.jpg",
     *   "message": "저희 두 사람의 새로운 출발에 함께해 주세요.",
     *   "contactInfo": "신랑 010-..., 신부 010-..."
     * }
     *
     * [응답 예시 JSON]
     * {
     *   "code": "aZ19lPxQ",
     *   "title": "...",
     *   "groomName": "...",
     *   ...
     * }
    * */
    @PostMapping
    public ResponseEntity<InvitationResponse> createInvitation(@RequestBody InvitationCreateRequest request){
        // 서비스 계층에 위임하여 초대장을 생성한다.
        InvitationResponse response = invitationService.createInvitation(request);

        // 200 OK or 201 Created 중 선택 가능하다.
        // 여기선 단순하게 200OK로 반환
        return ResponseEntity.ok(response);
    }

    /**
     * 초대장 조회 API
     * ------------------------------------------------
     * [HTTP]
     *   GET /api/v1/invitations/{code}
     *
     *  예)
     *   GET /api/v1/invitations/aZ19lPxQ
     *
     *  - 모바일 청첩장 페이지 진입 시,
     *    해당 code 로 초대장 정보를 조회할 때 사용
     */
    @GetMapping("/{code}")
    public ResponseEntity<InvitationResponse> getInvitation(@PathVariable("code") String code) {
        // 서비스 계층에서 code로 초대장을 조회해서 초대장을 보여준다.
        InvitationResponse response = invitationService.getInvitationByCode(code);
        return ResponseEntity.ok(response);
    }

    /*
    * 갤러리 이미지 조회
    * /api/v1/invitations/{code}/images
    * 이미지 리스트에서 이미지 url만 뽑아서 내려다 준다.
    * */
    @GetMapping("/{code}/images")
    public ResponseEntity<List<String>> getInvitationImages(@PathVariable("code") String code){
        List<InvitationImage> images = invitationImageService.getImagesByInvitationCode(code);

        List<String> imageUrls = images.stream().map(InvitationImage::getImageUrl).toList();

        return ResponseEntity.ok(imageUrls);
    }


}
