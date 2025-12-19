package com.eggmoney.payv.infrastructure.mybatis.record;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 *  Record: CommentRecord
 * - DB 테이블과 1:1 매핑되는 데이터 객체.
 * - MyBatis Mapper에서 반환/입력되는 데이터 구조.
 * - Domain 엔티티(Comment)와 변환 과정 필요.
 * 
 * @author 한지원
 *
 */
@Data
@Builder
public class CommentRecord {
	private String commentId;
	private String boardId;
	private String userId;
	private String content;
    private String visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}