package com.example.toyproject.security;
/*
* 작성자 : 조정학
* 작성일 : 20251003
* */
import com.example.toyproject.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/*
SecurityConfig: “규칙(인가/로그인 페이지/세션)”을 정의
CustomUserDetailsService: “사용자 조회(인증 대상)”를 정의
* 로그인 시 아이디(id)로 DB에서 사용자를 조회하는 역할
*  - 스프링 시큐리티는 내부적으로  UserDetailsService를 호출해 사용자 정보를 가져온 뒤
*      비밀번호(BCrypt) 매칭을 수행하고, 성공하면 세션에 인증객체를 저장한다.
* */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 생성자 의존성 주입
    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }


    /**
     * (질문1) 해당 함수는 내부 함수클래스이고, 로그인한 id, pw를 db 값과 비교해서 맞으면 로그인인증 성공)
     * 파라미터 username은 login.html에서 보낸 name="username" 값.
     * 우리는 User 엔티티의 PK(id)를 username으로 사용한다.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findById(username)
                .map(u ->
                        // 스프링 시큐리티가 이해할 수 있는 UserDetails 객체로 변환
                        new org.springframework.security.core.userdetails.User(
                                u.getId(),                     // username
                                u.getPassword(),               // BCrypt 해시 저장된 비번
                                List.of(new SimpleGrantedAuthority(u.getRole())) // 예: "ROLE_USER"
                        )
                )
                .orElseThrow(() -> new UsernameNotFoundException("user not found: " + username));
    }
}
