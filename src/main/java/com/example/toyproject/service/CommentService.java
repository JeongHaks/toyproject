package com.example.toyproject.service;

/*
 * 작성자 : 조정학
 * 작성일 : 2025-10-24
 */

import com.example.toyproject.domain.Comment;
import com.example.toyproject.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    /** ✅ 목록 조회: 부모 바로 아래 정렬 (postId = String) */
    @Transactional(readOnly = true)
    public List<Comment> listForPost(String postId) {
        if (postId == null || postId.isBlank()) {
            throw new IllegalArgumentException("postId는 필수입니다.");
        }
        return commentRepository.findAllForPostOrdered(postId);
    }

    /** ✅ 루트 댓글 작성: groupId = 자기 id, orderInGroup = 0 */
    @Transactional
    public Comment addRootComment(String postId, String userId, String content) {
        if (postId == null || postId.isBlank()) throw new IllegalArgumentException("postId는 필수입니다.");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId는 필수입니다.");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content는 비어있을 수 없습니다.");

        String newId =  java.util.UUID.randomUUID().toString();

        Comment root = Comment.builder()
                .id(newId)                 // ← String (UUID 문자열)
                .postId(postId)            // ← String
                .userId(userId)
                .content(content.trim())
                .parentId(null)
                .build();

        root.setGroupId(newId);            // 루트는 자기 자신을 그룹으로
        root.setOrderInGroup(0);           // 루트는 0
        return commentRepository.save(root);
    }

    /** ✅ 새로 추가 : 대댓글 (부모 바로 아래 삽입) */
    @Transactional
    public Comment addChildComment(String postId, String parentId, String userId, String content) {
        Comment parent = commentRepository.findByIdForUpdate(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));

        // 부모와 동일한 group 내에서 뒤쪽 순서를 +1씩 밀기
        commentRepository.shiftOrders(parent.getGroupId(), parent.getOrderInGroup());

        // 부모 바로 다음에 새 대댓글 삽입
        Comment reply = new Comment();
        reply.setId(UUID.randomUUID().toString());
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.setParentId(parent.getId());
        reply.setGroupId(parent.getGroupId());
        reply.setOrderInGroup(parent.getOrderInGroup() + 1);
        reply.setCreatedAt(LocalDateTime.now());

        commentRepository.save(reply);
        return reply;
    }



    /** ✅ 대댓글 작성: 부모 바로 아래로 끼워 넣기 (shift → insert) */
    @Transactional
    public Comment addReply(String parentId, String userId, String content) {
        if (parentId == null || parentId.isBlank()) throw new IllegalArgumentException("parentId는 필수입니다.");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId는 필수입니다.");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content는 비어있을 수 없습니다.");

        // 1) 부모 잠금 (String id)
        Comment parent = commentRepository.findByIdForUpdate(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

        // 2) 부모 뒤쪽 순서 +1로 밀기
        commentRepository.shiftOrders(parent.getGroupId(), parent.getOrderInGroup());

        // 3) 자식 삽입 (부모 group 상속, 부모 바로 아래)
        String newId = UUID.randomUUID().toString();

        Comment child = Comment.builder()
                .id(newId)                         // ← String
                .postId(parent.getPostId())        // 부모의 postId 상속 (String)
                .userId(userId)
                .content(content.trim())
                .parentId(parent.getId())          // 부모 id (String)
                .build();

        child.setGroupId(parent.getGroupId());                   // 같은 그룹
        child.setOrderInGroup(parent.getOrderInGroup() + 1);     // 부모 바로 아래

        return commentRepository.save(child);
    }

    /** ✍️ 수정 (작성자만) — 모든 ID는 String */
    @Transactional
    public void updateComment(String commentId, String requesterUserId, String newContent) {
        if (commentId == null || commentId.isBlank()) throw new IllegalArgumentException("commentId는 필수입니다.");
        if (requesterUserId == null || requesterUserId.isBlank()) throw new IllegalArgumentException("userId는 필수입니다.");
        if (newContent == null || newContent.isBlank()) throw new IllegalArgumentException("내용을 입력하세요.");

        Comment target = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!target.getUserId().equals(requesterUserId)) {
            throw new SecurityException("본인 댓글만 수정할 수 있습니다.");
        }

        target.setContent(newContent.trim()); // dirty checking
    }

    /** 🗑️ 삭제 (작성자만, 자식 없을 때) — 모든 ID는 String */
    @Transactional
    public void deleteComment(String commentId, String requesterUserId) {
        if (commentId == null || commentId.isBlank()) throw new IllegalArgumentException("commentId는 필수입니다.");
        if (requesterUserId == null || requesterUserId.isBlank()) throw new IllegalArgumentException("userId는 필수입니다.");

        Comment target = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!target.getUserId().equals(requesterUserId)) {
            throw new SecurityException("본인 댓글만 삭제할 수 있습니다.");
        }

        long childCount = commentRepository.countByParentId(target.getId());
        if (childCount > 0) {
            throw new IllegalStateException("대댓글이 있어 삭제할 수 없습니다.");
        }

        commentRepository.delete(target);
    }

    /** 🔁 (옵션) 기존 컨트롤러 호환용: postid(String)로 목록 조회 */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPost(String postid) {
        if (postid == null || postid.isBlank()) {
            throw new IllegalArgumentException("postid는 필수입니다.");
        }
        // 이제 listForPost가 String을 받으니 바로 호출
        return listForPost(postid);
    }
}
