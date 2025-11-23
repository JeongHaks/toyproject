package com.example.toyproject.auth;

/*
* 작성자 : 조정학
* 작성일 : 20250929
* */

import com.example.toyproject.auth.dto.SignupRequest;
import com.example.toyproject.domain.User;
import com.example.toyproject.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/*
* AuthService
*  : 회원가입 관련 비즈니스 로직을 담당하는 클래스이다.
*  : Controller에서 받은 요청을 실제로 처리하는 곳이다. (중복 검사, 비밀번호 해시, 저장 등등)
* */
@Service
public class AuthService {

    private final UserRepository userRepository; // DB 접근용 JPA Repository
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); // 비밀번호 해시 및 검증용 객체

    // 생성자 주입 하기
    public AuthService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // 회원가입시 처리 메서드
    public String signup(SignupRequest req){
        // ID 중복검사
        if(userRepository.existsById(req.id)){
            throw new IllegalArgumentException("이미 존재하는 ID 입니다.");
        }

        // 닉네임 중복 검사
        if(userRepository.existsByNickname(req.nickname)){
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // User 엔티티 생성 및 값 설정하는 곳
        // User 객체 생성
        User user = new User();
        user.setId(req.id);
        // 비밀번호는 반드시 해시(암호화)해서 저장해야 한다
        user.setPassword(encoder.encode(req.password));
        user.setNickname(req.nickname);
        // role, created_at은 User 엔티티에 기본값 세팅되어있다.

        // DB에 저장하기.
        userRepository.save(user);

        // 회원가입한 User Id 정보를 가져온다.
        return user.getId();
    }
}
