package com.example.toyproject.config;

/*
* 작성자 : 조정학
* 작성일 : 20251002
* */

/*
SecurityConfig: “규칙(인가/로그인 페이지/세션)”을 정의
CustomUserDetailsService: “사용자 조회(인증 대상)”를 정의
 * 스프링 시큐리티 핵심 설정 클래스 부분
 *  - 어떤 url은 로그인 없이 허용할지(permitAll)
 *  - 어떤 url은 로그인 후에만 접근할지(authenticated)
 *  - 폼 로그인 페이지 경로 설정
 *  - 비밀번호 Encoder(BCrypt) 등록
 **/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
public class SecurityConfig {
    /*
    * HTTP 통신 보안 규칙 설정
    *  - /login, /auth/signup, 정적리소스는 누구나 접근 가능
    *  - 그 외는 인증 필요
    *  - formLogin() : 우리가 만든 login.html을 사용해서 로그인 처리
    * */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                // URL 접근 권한 (로그인 없이 접근 허용 가능한 URL)
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/","/login","/auth/signup","/css/**","/js/**","/images/**").permitAll() // 로그인 없이 접근 허용
                .requestMatchers(HttpMethod.GET, "/posts", "/posts/*").permitAll() // 목록/상세 공개
                .anyRequest().authenticated() // 이 외 로그인 인증 필요
                )
                // 폼 로그인 설정(질문5) 폼 로그인 의미
                .formLogin(login -> login
                .loginPage("/login") // GET /login --> login.html 렌더(우리가 만든 페이지)
                .loginProcessingUrl("/login") //POST /login --> 시큐리티가 인증 처리 해준다.(인증 완료하면 아래 페이지로 이동)
                .defaultSuccessUrl("/", true) // 로그인 성공 후 home.html 이동 경로 (게시글 작성 페이지...수정)
                .permitAll()
                )
                // 로그아웃은 기본값 사용 (/logout)
                .logout(Customizer.withDefaults());

        
        // CSRF는 기본 활성화(권장). 폼에 hidden 토큰만 넣으면 됩니다.(질문6) CSRF 개념 설명
        return http.build();
    }

    /* 비밀번호 인코더
     * - 회원가입 때 저장한 BCrypt 해시와
     *   로그인 때 입력한 비밀번호를 비교할 때 사용
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
