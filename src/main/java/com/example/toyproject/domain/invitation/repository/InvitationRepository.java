package com.example.toyproject.domain.invitation.repository;

import com.example.toyproject.domain.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// InvitationRepository
/*
 *  - 초대장 데이터를 DB와 연결해주는 인터페이스 클래스이다.
 *  - JPA가 자동으로 CRUD 메서드를 만들어 준다.
 *  - findByCode(), existByCode() 는 우리가 직접 정의한 맞춤 조회 메서드
 * */
public interface InvitationRepository extends JpaRepository<Invitation,Long> {
    /*
     * 공유 URL을 코드(Code)로 초대장을 찾는 메서드이다.
     * ex) /invitation/a1b2c3 --> code = "a1b2c3"
     * */
    Optional<Invitation> findByCode(String code);

    /*
     * 코드 중복 여부 확인하는 메서드
     *  - 초대장 생성할 때 동일한 code가 이미 존재하는지 체크할 때 사용한다.
     * */
     boolean existsByCode(String code);

}