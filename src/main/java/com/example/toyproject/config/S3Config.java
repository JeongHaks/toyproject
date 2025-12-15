package com.example.toyproject.config;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class S3Config {
    //@Value("${AWS_REGION}")
    @Value("${cloud.aws.region.static}")
    private String region;
    // Render에서는 ENV로 들어오고, 로컬에서는 비어있으면 properties에 등록해도 되고
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Bean
    public AmazonS3 amazonS3() {

        // 1) env / properties 에서 못 읽어오면 에러 던져서 빨리 알 수 있게
        if (accessKey == null || accessKey.isBlank() ||
                secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY 설정을 확인해주세요.");
        }

        // 2) 기본 자격증명 생성 (EC2 메타데이터 안 탐)
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

        // 3) S3 클라이언트 생성
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }
}
