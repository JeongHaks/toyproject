package com.example.toyproject.rag.chunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkSqlitter {

    private ChunkSqlitter() {}

    public static List<String> splitToChunks(String content, int chunkSize, int overlap) {
        System.out.println("파일 업로드 후 텍스트 추출 및 chunk(문장 나누기) 1 : ");
        // 1) 입력 검증
        if (content == null || content.isBlank()) {
            return List.of();
        }
        // 문장 길이기 0보다 작을 경우 오류 문구 전송
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be > 0");
        }
        // overlap 길이 조건이 0보다 작거나 문장길이보다 길 경우 오류 문구 전송
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must satisfy 0 <= overlap < chunkSize");
        }

        // 2) 분할 로직
        List<String> chunks = new ArrayList<>();
        // 문서 내용
        int length = content.length();
        int start = 0;

        // 오버랩 기준으로 청크를 활용해서 문장을 쪼개는 역할을 한다.
        while (start < length) {
            // chunk 내용과
            int end = Math.min(start + chunkSize, length);
            // 문장을 잘라서 chunks 리스트에 추가.
            chunks.add(content.substring(start, end));

            // 최소 문자열 길이와 content 내용 길이와 동일하면 stop
            if (end == length) {
                break;
            }
            // overlap 만큼 겹치게 다음 시작점 이동
            // 0~800, end = 800 , overlap = 200 이면 start 600부터
            // 겹쳐야 내용이 안 끊어지고, 이어지게 결과 표출
            start = end - overlap;
        }

        return chunks;
    }
}