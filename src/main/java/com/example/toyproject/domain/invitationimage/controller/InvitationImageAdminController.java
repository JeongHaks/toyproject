package com.example.toyproject.domain.invitationimage.controller;


import com.example.toyproject.domain.invitation.entity.Invitation;
import com.example.toyproject.domain.invitation.service.InvitationService;
import com.example.toyproject.domain.invitationimage.service.InvitationImageService;
import com.example.toyproject.service.S3Uploader;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

//@Profile("prod") // 추가 작성(로컬 전용)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/invitations")
public class InvitationImageAdminController {

    private final InvitationService invitationService;
    private final InvitationImageService invitationImageService;
    private final S3Uploader s3Uploader;

    @PostMapping("/{code}/images")
    public ResponseEntity<String> uploadImages(@PathVariable("code") String code,
                                               @RequestParam("files") MultipartFile[] files) throws Exception {

        Invitation invitation = invitationService.getInvitationEntityByCode(code);

        int sortOrder = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // S3에 업로드
            String imageUrl = s3Uploader.upload(file, "gallery");

            // DB 저장
            invitationImageService.saveImage(invitation, imageUrl, sortOrder++);
        }

        return ResponseEntity.ok("이미지 업로드 성공!! (files=" + files.length + ")");
    }
}
