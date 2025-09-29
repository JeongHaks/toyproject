package com.example.toyproject.auth;

/*
* 작성자 : 조정학
* 작성일 : 20250929
* */

/*
* 회원가입 API 컨트롤러
*  : URL - POST /api/auth/signup
*  : 요청Body - SignupRequest (id, password, nickname)
*  : 응답 - 201 Created + 생성된 사용자 id (문자열)
*
* 컨트롤러의  책임
*  : HTTP 요청(JSON)을 DTO로 받는다.
*  : 서비스에서 회원가입 처리를 위임한다.
*  : 결과를 적절한 HTTP 상태코드로 반환한다.
* */

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authservice;

    // 생성자 주입 (스프링이 AuthService 빈을 자동 주입
    public AuthController(AuthService authservice){
        this.authservice = authservice;
    }

    // 회원가입 엔드포인트(질문4)
    /*
    * 성공 : 201 Created + Location 헤더(/api/users/{id} ) + body에 id
    * 실패(중복 등) : 400 Bad Request
    * */
    //질문5
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest req){
        String createdId = authservice.signup(req);

        // Location 헤더 예시(선택) : 나중에 /api/users/{id} 같은 조회 API가 생기면 유용하다.(질문6)
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, "/api/users/" + createdId);

        return new ResponseEntity<>(createdId, headers, HttpStatus.CREATED);
    }

    /**
     * 간단한 에러 매핑
     * - 서비스에서 IllegalArgumentException 던질 때(중복 ID/닉네임 등) 400으로 내려줌
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArg(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
