package com.eggmoney.payv.domain.model.repository;

import java.util.List;
import java.util.Optional;

import com.eggmoney.payv.domain.model.entity.Comment;
import com.eggmoney.payv.domain.model.vo.BoardId;
import com.eggmoney.payv.domain.model.vo.CommentId;

/**
 * Repository Interface: CommentRepository 
 * - 책임: Comment 엔티티 영속성 관리.
 * - 인터페이스만 정의하여 구현(MyBatis, JPA, Memory 등)은 인프라 레이어에서 제공.
 * 
 * @author 한지원
 *
 */
public interface CommentRepository {
    Optional<Comment> findById(CommentId id);
    List<Comment> findByBoard(BoardId boardId);
    void save(Comment comment);
    void delete(CommentId id);
}

