package com.example.toyproject.domain.invitationimage.repository;

import com.example.toyproject.domain.invitationimage.entity.InvitationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface InvitationImageRepository extends JpaRepository<InvitationImage,Long> {

    // 초대장 code 기준으로 갤러리 이미지 조회 후 정렬해서 표현
    List<InvitationImage> findByInvitation_CodeOrderBySortOrderAsc(String code);

}
