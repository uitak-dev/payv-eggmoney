package com.eggmoney.payv.domain.model.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.eggmoney.payv.domain.model.vo.BoardId;
import com.eggmoney.payv.domain.model.vo.CommentId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Comment {
    private CommentId id;
    private BoardId boardId;
    private UserId userId;
    private String content;
    private Visibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Comment(CommentId id, BoardId boardId, UserId userId, String content,
                   Visibility visibility, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.boardId = boardId;
        this.userId = userId;
        this.content = content;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Comment create(BoardId boardId, UserId userId, String content) {
        return Comment.builder()
                .id(CommentId.of(EntityIdentifier.generateUuid()))
                .boardId(boardId)
                .userId(userId)
                .content(content)
                .visibility(Visibility.PUBLIC)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public String getCreatedAtText() {
        if (this.createdAt == null) return "";
        return this.createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }


    public void update(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }
}
