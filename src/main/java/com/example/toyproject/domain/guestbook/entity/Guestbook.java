package com.example.toyproject.domain.invitation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name="guestbook")
@AllArgsConstructor
@Builder
public class Guestbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id")
    private Invitation invitation;

    private String guestName;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 방명록 엔티티 생성 편의 메서드
     */
    public static Guestbook create(Invitation invitation, String guestName, String message) {
        Guestbook guestbook = new Guestbook();
        guestbook.invitation = invitation;
        guestbook.guestName = guestName;
        guestbook.message = message;
        return guestbook;
    }
}
