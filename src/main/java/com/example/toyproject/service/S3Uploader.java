package com.example.toyproject.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    //@Value("${S3_BUCKET_NAME}")
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 파일을 S3에 업로드하고, 접근 가능한 URL을 반환한다.
     *
     * @param file    업로드할 파일 (MultipartFile)
     * @param dirName S3 버킷 안의 폴더명 (예: "main", "gallery")
     * @return 업로드된 파일의 S3 URL
     */
    public String upload(MultipartFile file, String dirName) throws IOException {
        // 1) 파일명 안전하게 가공
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "image";
        }

        // 확장자 분리 (예: .jpg, .png)
        String ext = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex != -1) {
            ext = originalFilename.substring(dotIndex);
        }

        // UUID_원본파일명 형태로 저장 (중복 방지)
        String uuid = UUID.randomUUID().toString();
        String encodedName = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
        String fileName = dirName + "/" + uuid + "_" + encodedName;   // 예: gallery/uuid_파일명.jpg

        // 2) 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // 3) S3에 업로드
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    inputStream,
                    metadata
            );

            amazonS3.putObject(putObjectRequest);
        }

        // 4) 업로드된 파일의 public URL 반환
        return amazonS3.getUrl(bucketName, fileName).toString();
    }
}