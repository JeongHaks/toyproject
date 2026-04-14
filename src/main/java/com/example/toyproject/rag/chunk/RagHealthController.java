package com.example.toyproject.rag.chunk;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*분할 DB 적용 후 연결 잘 되는지 확인을 위한 HealthController 작성 */
@RestController
public class RagHealthController {

    private final JdbcTemplate ragJdbcTemplate;
    private final JdbcTemplate mainJdbcTemplate;

    public RagHealthController(@Qualifier("ragJdbcTemplate") JdbcTemplate ragJdbcTemplate
                              ,JdbcTemplate mainJdbcTemplate){ // 기본 DataSource(운영DB)로 자동 생성됨
        this.ragJdbcTemplate = ragJdbcTemplate; // RAG 전용 DB 연결 확인
        this.mainJdbcTemplate = mainJdbcTemplate; // 기존 게시판 DB 연결 확인
    }

    @GetMapping("/health/db")
    public String health() {
        Integer mainOk = mainJdbcTemplate.queryForObject("select 1", Integer.class);
        Integer ragOk = ragJdbcTemplate.queryForObject("select 1", Integer.class);
        return "main=" + mainOk + ", rag=" + ragOk;
    }
}
