package com.example.toyproject.domain.invitationimage.entity;

import com.example.toyproject.domain.invitation.entity.Invitation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "invitation_image")
public class InvitationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 초대장(부모)과의 다대일 관계: N(이미지) : 1(초대장)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;

    // 이미지 URL (정적 경로, S3, etc)
    @Column(nullable = false, length = 1000)
    private String imageUrl;

    // 정렬 순서
    @Column(nullable = false)
    private int sortOrder = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 정적 팩토리 메서드
    public static InvitationImage create(Invitation invitation, String imageUrl, int sortOrder) {
        InvitationImage image = new InvitationImage();
        image.invitation = invitation;
        image.imageUrl = imageUrl;
        image.sortOrder = sortOrder;
        return image;
    }
}
