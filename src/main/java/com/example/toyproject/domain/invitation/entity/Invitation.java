package com.example.toyproject.domain.invitation.entity;


import com.example.toyproject.domain.invitationimage.entity.InvitationImage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Invitation Entity 클래스 생성
@Entity
@Table(
        name = "invitation",
        indexes = {
                @Index(name = "idx_invitation_code", columnList = "code", unique = true)
        }
      )
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL BIGSERIAL 대응
    private Long id;

    // 공유 URL용 코드 (예: a1b2c3d4)
    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;

    // 청첩장 제목
    @Column(name = "title", nullable = false)
    private String title;

    // 신랑 / 신부 이름
    @Column(name = "groom_name", nullable = false)
    private String groomName;

    @Column(name = "bride_name", nullable = false)
    private String brideName;

    // 예식 일자 / 시간
    @Column(name = "wedding_date", nullable = false)
    private LocalDate weddingDate;

    @Column(name = "wedding_time", nullable = false)
    private LocalTime weddingTime;

    // 예식장 정보
    @Column(name = "hall_name", nullable = false)
    private String hallName;   // 예: OO웨딩컨벤션 3층 그랜드홀

    @Column(name = "address", nullable = false)
    private String address;    // 예: 서울시 OO구 OO로 123

    // 지도 링크 (카카오맵, 네이버맵 등)
    @Column(name = "map_url", length = 1000)
    private String mapUrl;

    // 메인 이미지 URL (가장 대표 사진 1장)
    @Column(name = "main_image_url", nullable = true, length = 1000)
    private String mainImageUrl;

    // 인사말 / 안내문
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    // 연락처 (신랑/신부/혼주 등 문자열로 간단히)
    @Column(name = "contact_info", length = 1000)
    private String contactInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvitationImage> images = new ArrayList<>();
}
