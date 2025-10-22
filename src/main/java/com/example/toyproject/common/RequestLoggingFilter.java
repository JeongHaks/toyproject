package com.example.toyproject.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * RequestLoggingFilter
 *
 * [역할]
 * - 모든 요청/응답을 관통하며 핵심 정보를 한 줄로 남긴다.
 * - 운영 중 이슈(느림/오류) 발생 시 역추적이 용이하다.
 *
 * [남기는 정보]
 * - requestId(요청 식별자, UUID) : 한 요청의 전체 흐름을 묶는 키
 * - HTTP Method, URI, Status, 처리시간(ms)
 * - 사용자명(로그인 상태일 때), 클라이언트 IP
 *
 * [주의]
 * - 민감정보(쿠키/토큰/본문)는 기록하지 않는다.
 */
@Slf4j
@Component // 컴포넌트 스캔으로 자동 등록
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // 요청 식별자를 생성하여 MDC(진행 중 로그 문맥)에 보관 → 같은 요청 내 모든 로그에 공통으로 찍힘
        String requestId = UUID.randomUUID().toString().substring(0, 8); // 짧게 8자리
        MDC.put("requestId", requestId);

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = getClientIp(request);

        try {
            // 체인 계속 진행 (컨트롤러/필터/핸들러 등으로 이동)
            chain.doFilter(request, response);
        } finally {
            long tookMs = System.currentTimeMillis() - start;

            // 현재 로그인 사용자명(없으면 "anonymous")
            String user = (request.getUserPrincipal() != null)
                    ? request.getUserPrincipal().getName()
                    : "anonymous";

            int status = response.getStatus();

            // 한 줄 로그: 운영에서 가장 자주 보는 라인
            log.info("rid={} {} {} -> {} ({}ms) user={} ip={}",
                    requestId, method, uri, status, tookMs, user, clientIp);

            // MDC 정리 (쓰레드 재사용 환경에서 누수 방지)
            MDC.clear();
        }
    }

    /** 프록시/로드밸런서 환경을 고려한 클라이언트 IP 추출 (간단 버전) */
    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For: "client, proxy1, proxy2" 형태 → 첫 번째가 실제 클라이언트
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
