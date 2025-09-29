package com.example.toyproject.repository;

import com.example.toyproject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/*
* 작성자 : 조정학
* 작성일 : 20250929
* */

/*
* 설명
*  : JPA를 사용해 DB 접근을 담당하는 인터페이스이다.
*  : JpaRepository<User, String> - 엔티티 타입(User), PK ID 타입 (String)
* */
public interface UserRepository extends JpaRepository<User, String> {

    // 특정 ID가 이미 존재하는지 확인하기 위한 함수
    boolean existsById(String id);

    // 특정 닉네임 존재 확인
    boolean existsByNickname(String nickname);

    // 질문1) exists라는 함수가 있는가??

}
