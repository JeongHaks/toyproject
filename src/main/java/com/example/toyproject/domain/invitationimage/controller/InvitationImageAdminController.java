package com.example.toyproject.domain.invitationimage.controller;


import com.example.toyproject.domain.invitation.entity.Invitation;
import com.example.toyproject.domain.invitation.service.InvitationService;
import com.example.toyproject.domain.invitationimage.service.InvitationImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/invitations")
public class InvitationImageAdminController {

    private final InvitationService invitationService;
    private final InvitationImageService invitationImageService;

    // 프로젝트 루트 기준 uploads 폴더
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /*
     * 갤러리 이미지 업로드 (관리자용)
     * /admin/invitations/{code}/images
     * */
    @PostMapping("/{code}/images")
    public ResponseEntity<String> uploadImages(@PathVariable("code") String code
            , @RequestParam("files") MultipartFile[] files) throws Exception {

        // 1) code로 초대장 엔티티 찾기
        Invitation invitation = invitationService.getInvitationEntityByCode(code);

        // 2) 업로드 폴더 없으면 생성
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        int sortOrder = 0;

        // 3) 넘어온 파일들 반복 처리
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            // 원본 파일명에서 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // UUID로 새 파일명 생성
            String newFilename = UUID.randomUUID().toString() + ext;

            // 실제 저장 경로
            Path savePath = uploadPath.resolve(newFilename);

            // 파일 저장
            file.transferTo(savePath.toFile());

            // 웹에서 접근할 URL (정적 리소스 기준)
            String imageUrl = "/uploads/" + newFilename;

            // DB 저장 (InvitationImage 엔티티)
            invitationImageService.saveImage(invitation, imageUrl, sortOrder++);
        }

        return ResponseEntity.ok("이미지 업로드 성공 (files=" + files.length + ")");
    }
}
