package com.example.toyproject.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

/*
 * 작업일자 : 20250928
 * 작성자 : 조정학
 * 설명 : DB에 생성하는 테이블 정의
 * */

@Entity
@Table(name="users")
@Getter
@Setter
public class User {

    //id, password, nickname, role, created_at
    @Id
    private String id;

    // null값을 허용하지 않겠다.
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable=false, length=20)
    private String role = "ROLE_USER";

    // 가입한 날짜 및 시간을 넣으려고 만든 변수
    private LocalDateTime created_at = LocalDateTime.now();
}
