package com.example.toyproject.domain.guestbook.repository;

import com.example.toyproject.domain.invitation.entity.Guestbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

    List<Guestbook> findByInvitation_CodeOrderByCreatedAtDesc(String code);

    // 방명록 삭제용 함수
    Optional<Guestbook> findByIdAndInvitation_Code(Long id, String code);
}
