package com.example.toyproject.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // InvitationImageAdminController와 동일한 기준 경로
    //private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // Render 기준
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 /uploads/** 로 접근하면
        // 실제 디스크의 UPLOAD_DIR에서 파일을 찾도록 매핑
        registry.addResourceHandler("/uploads/**")
                //.addResourceLocations("file:" + UPLOAD_DIR);
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
