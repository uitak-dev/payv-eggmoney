package com.eggmoney.payv.domain.model.entity;

import java.time.LocalDateTime;

import com.eggmoney.payv.domain.model.vo.BoardId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

import lombok.Builder;
import lombok.Getter;

/**
 * Aggregate: Board
 * - 책임: 게시글 엔티티 상태 관리.
 * - 게시글 식별자(BoardId), 작성자(UserId), 제목/내용, 조회수, 생성/수정 일시 등 보관.
 * - create() 정적 팩토리 메서드로만 생성 가능 (무결성 보장).
 * 
 * 불변성:
 * - id, userId는 생성 후 변경 불가.
 * - 제목(title)은 반드시 존재해야 함.
 * 
 * @author 한지원
 *
 */
@Getter
public class Board {

	private BoardId id;
    private UserId userId;
    private BoardType type;
    private String title;
    private String content;
    private Visibility visibility;
    private long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Board(BoardId id, UserId userId, BoardType type, String title, String content, Visibility visibility, long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.visibility = visibility;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 게시글 생성 팩토리 메서드
    // - BoardId는 UUID 자동 생성
    public static Board create(UserId userId, String title, String content) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("title is required");

        return Board.builder()
                .id(BoardId.of(EntityIdentifier.generateUuid()))
                .userId(userId)
                .type(BoardType.GENERAL)	//기본 Gerneral
                .title(title)
                .content(content)
                .visibility(Visibility.PUBLIC)	//기본 PUBLIC
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 게시글 수정
    // - 제목 필수
    // - 내용은 비어있을 수 있음
    // - 수정 시 updatedAt 갱신
    public void update(String newTitle, String newContent) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("title is required");
        }
        this.title = newTitle.trim();
        this.content = newContent; // 내용은 비어있을 수 있다고 가정
        this.updatedAt = LocalDateTime.now();
    }
}