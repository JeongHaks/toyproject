package com.example.toyproject.domain.invitation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;


/**
 * InvitationCreateRequest
 * -------------------------------------------
 * - 초대장(Invitation)을 새로 생성할 때
 *   클라이언트(관리자 화면)에서 전달받는 요청 DTO
 * - Controller → Service 로 데이터를 전달할 때 사용
 */
@Getter
@NoArgsConstructor
public class InvitationCreateRequest {
    // 청첩장 제목
    private String title;

    // 신랑 / 신부 이름
    private String groomName;
    private String brideName;

    // 예식 일자 / 시간
    private LocalDate weddingDate;
    private LocalTime weddingTime;

    // 예식장 정보
    private String hallName;   // 예: OO웨딩컨벤션 3층 그랜드홀
    private String address;    // 예: 서울시 OO구 OO로 123

    // 지도 링크 (카카오맵, 네이버맵 등)
    private String mapUrl;

    // 메인 이미지 URL (대표 사진 1장)
    private String mainImageUrl;

    // 인사말 / 안내문
    private String message;

    // 연락처 (신랑/신부/혼주 등)
    private String contactInfo;

}
