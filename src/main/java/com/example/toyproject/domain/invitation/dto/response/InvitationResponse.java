package com.example.toyproject.domain.invitation.dto.response;

import com.example.toyproject.domain.invitation.entity.Invitation;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * InvitationResponse
 * -----------------------------------------------
 * - 초대장 정보를 클라이언트(관리자/모바일)에 반환할 때 사용하는 DTO
 * - Entity 전체를 그대로 노출하지 않고
 *   필요한 필드만 선택적으로 내려주는 목적
 */
@Getter
public class InvitationResponse {

    private final String code;            // 공유 URL code
    private final String title;

    private final String groomName;
    private final String brideName;

    private final String weddingDate;
    private final String weddingTime;

    private final String hallName;
    private final String address;

    private final String mapUrl;
    private final String mainImageUrl;

    private final String message;
    private final String contactInfo;

    /**
     * Entity → Response DTO 변환 생성자
     * Entity는 DB 구조, 외부로 절대로 노출하면 안 된다(보안/안정성)
     * Entity는 그대로 노출하면 JPA Lazy 로딩 문제가 터질 수도 있다.
     */
    public InvitationResponse(Invitation invitation) {
        this.code = invitation.getCode();
        this.title = invitation.getTitle();

        this.groomName = invitation.getGroomName();
        this.brideName = invitation.getBrideName();

        // 🔥 null-safe 변환 (null이면 null 그대로 내려감)
        this.weddingDate = Optional.ofNullable(invitation.getWeddingDate())
                .map(LocalDate::toString)
                .orElse(null);

        this.weddingTime = Optional.ofNullable(invitation.getWeddingTime())
                .map(LocalTime::toString)
                .orElse(null);

        this.hallName = invitation.getHallName();
        this.address = invitation.getAddress();

        this.mapUrl = invitation.getMapUrl();
        this.mainImageUrl = invitation.getMainImageUrl();

        this.message = invitation.getMessage();
        this.contactInfo = invitation.getContactInfo();
    }
}
