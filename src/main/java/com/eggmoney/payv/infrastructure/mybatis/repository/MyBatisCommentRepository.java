package com.eggmoney.payv.infrastructure.mybatis.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.eggmoney.payv.domain.model.entity.Comment;
import com.eggmoney.payv.domain.model.entity.Visibility;
import com.eggmoney.payv.domain.model.repository.CommentRepository;
import com.eggmoney.payv.domain.model.vo.BoardId;
import com.eggmoney.payv.domain.model.vo.CommentId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.infrastructure.mybatis.mapper.CommentMapper;
import com.eggmoney.payv.infrastructure.mybatis.record.CommentRecord;

import lombok.RequiredArgsConstructor;

/**
 * Repository Implementation: MyBatisCommentRepository
 * - 책임: CommentRepository 인터페이스 구현 (MyBatis 기반).
 * - CommentRecord ↔ Comment 변환을 담당.
 * 
 * @author 한지원
 */
@Repository
@RequiredArgsConstructor
public class MyBatisCommentRepository implements CommentRepository {

    private final CommentMapper mapper;

    @Override
    public Optional<Comment> findById(CommentId id) {
        return Optional.ofNullable(mapper.selectById(id.value()))
                       .map(this::toDomain);
    }

    @Override
    public List<Comment> findByBoard(BoardId boardId) {
        return mapper.selectByBoard(boardId.value()).stream()
                     .map(this::toDomain)
                     .collect(Collectors.toList());
    }

    @Override
    public void save(Comment comment) {
        CommentRecord existing = mapper.selectById(comment.getId().value());
        if (existing == null) {
            mapper.insert(toRecord(comment));
        } else {
            mapper.update(toRecord(comment));
        }
    }

    @Override
    public void delete(CommentId id) {
        mapper.delete(id.value());
    }

    // === 변환 메서드 ===
    private Comment toDomain(CommentRecord record) {
        return Comment.builder()
                .id(CommentId.of(record.getCommentId()))
                .boardId(BoardId.of(record.getBoardId()))
                .userId(UserId.of(record.getUserId()))
                .content(record.getContent())
                .visibility(record.getVisibility() != null ? Visibility.valueOf(record.getVisibility()) : null)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private CommentRecord toRecord(Comment comment) {
        return CommentRecord.builder()
                .commentId(comment.getId().value())
                .boardId(comment.getBoardId().value())
                .userId(comment.getUserId().value())
                .content(comment.getContent())
                .visibility(comment.getVisibility() != null ? comment.getVisibility().name() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
