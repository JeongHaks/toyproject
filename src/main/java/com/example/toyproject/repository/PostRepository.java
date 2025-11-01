package com.example.toyproject.repository;

import com.example.toyproject.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 게시판 리포지토리
 * - JpaRepository<Post(엔티티타입), Long(엔티티 PK 타입)> 을 상속하면 기본 CRUD 메서드 자동 제공
 *   (save, findById, findAll, deleteById 등)
 * - 추가적으로 페이징/정렬 지원 메서드를 선언 가능
 * [ 자동 제공 메서드 예시: save(post) : 저장/수정, findById(id) : 단건 조회, findAll() : 전체 조회, deleteById(id) : 삭제 ]
 */
//(질문2) 글 목록 정렬을 위한 용도인가?
public interface PostRepository extends JpaRepository<Post, Long> {
    /**
     * 최신 글 목록순으로 페이징
     * ex) postRepo.findAllByOrderByIdDesc(PageRequest.of(0, 10))
     */
    Page<Post> findAllByOrderByIdDesc(Pageable pageable);
    /**
     * 특정 작성자 기준 목록순으로 페이징
     * ex) postRepo.findByUserIdOrderByIdDesc("hong", PageRequest.of(0, 10))
     */
    Page<Post> findByUserIdOrderByIdDesc(String userId, Pageable pageable);

    @Query(
            value = """
    SELECT p
    FROM Post p
    WHERE (:kw IS NULL OR :kw = '' 
           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :kw, '%'))
           OR LOWER(p.content) LIKE LOWER(CONCAT('%', :kw, '%'))
           OR LOWER(p.userId) LIKE LOWER(CONCAT('%', :kw, '%')))
    ORDER BY p.id DESC
  """,
            countQuery = """
    SELECT COUNT(p)
    FROM Post p
    WHERE (:kw IS NULL OR :kw = '' 
           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :kw, '%'))
           OR LOWER(p.content) LIKE LOWER(CONCAT('%', :kw, '%'))
           OR LOWER(p.userId) LIKE LOWER(CONCAT('%', :kw, '%')))
  """
    )
    Page<Post> search(@Param("kw") String keyword, Pageable pageable);
}
