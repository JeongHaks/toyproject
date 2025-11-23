package com.example.toyproject.domain.invitation.service;

import com.example.toyproject.domain.invitation.dto.request.InvitationCreateRequest;
import com.example.toyproject.domain.invitation.dto.response.InvitationResponse;
import com.example.toyproject.domain.invitation.entity.Invitation;
import com.example.toyproject.domain.invitation.repository.InvitationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

/**
 * InvitationService
 * -------------------------------------------
 * - 초대장(Invitation) 관련 비즈니스 로직을 처리하는 서비스 레이어
 * - 컨트롤러와 레포지토리 사이에서 중간 역할을 함
 * - 예)
 *      - Invitation 생성
 *      - Invitation 조회
 *      - code 중복 체크
 * - 지금은 뼈대만 만들고,
 *   실제 메서드는 다음 단계에서 하나씩 구현할 예정
 */
@Service
@RequiredArgsConstructor // final 필드를 자동으로 생성자 주입해준다.
public class InvitationService {

    // Repository 의존성 주입
    private final InvitationRepository invitationRepository;

    public List<Invitation> findAll() {
        return invitationRepository.findAll();
    }

    // 랜덤 코드 생성을 위한 문자 집합 변수 생성
    // 표준 랜덤 코드 문자 집합이다. (만국 공통)
    private static final String CODE_CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 초대장을 새로 생성하는 메서드
     * --------------------------------
     * 1. 요청 DTO(InvitationCreateRequest)를 받아서
     * 2. 유니크한 code 값을 생성한 뒤
     * 3. Entity(Invitation)를 만들어 저장하고
     * 4. InvitationResponse DTO로 변환해서 반환
     */
    @Transactional
    public InvitationResponse createInvitation(InvitationCreateRequest request){

        // 공유용 코드 생성(중복XX) 랜덤으로 생성한 코드값을 변수에 저장
        String code = generateUniqueCode();

        // DTO -> Entity로 변환
        Invitation invitation = Invitation.builder()
                .code(code)
                .title(request.getTitle())
                .groomName(request.getGroomName())
                .brideName(request.getBrideName())
                .weddingDate(request.getWeddingDate())
                .weddingTime(request.getWeddingTime())
                .hallName(request.getHallName())
                .address(request.getAddress())
                .mapUrl(request.getMapUrl())
                .mainImageUrl(request.getMainImageUrl())
                .message(request.getMessage())
                .contactInfo(request.getContactInfo())
                .build();

        // DB 저장한다.
        Invitation saved = invitationRepository.save(invitation);

        // Entity -> Response DTO 변환해서 반환한다.
        return new InvitationResponse(saved);

    }


    /**
     * 공유용 코드(code)로 초대장 조회하는 메서드
     * --------------------------------
     * - /invitation/{code} 진입할 때 사용 예정
     */
    @Transactional(readOnly = true)
    public InvitationResponse getInvitationByCode(String code) {
        Invitation invitation = invitationRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 초대장 코드입니다. code=" + code));

        return new InvitationResponse(invitation);
    }

    // code로 초대장 조회 / Entity용
    public Invitation getInvitationEntityByCode(String code) {
        return invitationRepository.findByCode(code).orElseThrow(()-> new IllegalArgumentException("초대장을 찾을 수 없습니다."));
    }


    /**
     * 유일한(중복되지 않는) 공유용 코드 생성
     * --------------------------------
     * - CODE_CHAR_SET에서 랜덤하게 CODE_LENGTH 길이만큼 뽑아서 문자열 생성
     * - DB에 같은 code가 이미 존재하면 다시 생성
     *
     *  예) aZ3kP9xQ 같은 형태
     */
    private String generateUniqueCode() {
        while (true) {
            String code = generateRandomCode();
            boolean exists = invitationRepository.existsByCode(code);
            if (!exists) {
                return code;
            }
            // 만약 중복이면 while 다시 돌면서 새로운 code 생성
        }
    }

    /**
     * 단순 랜덤 코드 생성 (중복 여부는 체크하지 않음)
     * return 코드값을 담아서 sb로 넘겨준다.
     * 코드가 단순하면 보안상 위험, 랜덤코드 생성해서 URL로 넘겨서 초대
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CODE_CHAR_SET.length());
            sb.append(CODE_CHAR_SET.charAt(index));
        }
        return sb.toString();
    }

    // 초대장 코드 기준으로 메인이미지 URL 설정
    @Transactional
    public void updateMainImageUrl(String code, String imageUrl) {
        Invitation invitation = invitationRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("초대장을 찾을 수 없습니다. code=" + code));

        invitation.setMainImageUrl(imageUrl);
        // @Transactional 이라서 별도 save 호출 없이도 변경사항이 반영됨
    }

}
