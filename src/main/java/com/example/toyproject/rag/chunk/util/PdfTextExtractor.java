package com.example.toyproject.rag.chunk.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * PDF 파일(InputStream) → 텍스트 추출 전용 클래스
 *
 * 역할:
 * - PDFBox를 사용해 PDF 내부 텍스트만 뽑는다
 * - 청킹/임베딩과 완전히 분리된 "순수 파싱" 단계
 */
@Component
public class PdfTextExtractor {
    // 업로드된 파일의 내용(텍스트) 추출 함수
    // pdf 파일 내용 추출
    public String extract(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document); // pdf 텍스트 내용 추출 후 return
        } catch (Exception e) {
            throw new IllegalStateException("PDF 텍스트 추출 실패", e);
        }
    }
}
