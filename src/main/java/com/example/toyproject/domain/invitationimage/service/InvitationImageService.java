package com.example.toyproject.domain.invitationimage.service;

import com.example.toyproject.domain.invitation.entity.Invitation;
import com.example.toyproject.domain.invitationimage.entity.InvitationImage;
import com.example.toyproject.domain.invitationimage.repository.InvitationImageRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // 생성자 자동 생성
public class InvitationImageService {

    private final InvitationImageRepository imageRepository;

    public List<InvitationImage> getImagesByInvitationCode(String code){
        return imageRepository.findByInvitation_CodeOrderBySortOrderAsc(code);
    }

    // 이미지 업로드 기능
    public void saveImage(Invitation invitation, String url, int sortOrder){
        InvitationImage image = InvitationImage.create(invitation,url,sortOrder);
        imageRepository.save(image);
    }

}
