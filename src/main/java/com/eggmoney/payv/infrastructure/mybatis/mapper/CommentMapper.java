package com.eggmoney.payv.infrastructure.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eggmoney.payv.infrastructure.mybatis.record.CommentRecord;

/**
 *  * MyBatis Mapper: CommentMapper
 * - DB와 직접 SQL 매핑 담당.
 * - CommentRecord를 기준으로 CRUD 수행.
 * 
 * @author 한지원
 *
 */
@Mapper
public interface CommentMapper {
	CommentRecord selectById(@Param("commentId") String commentId);

	// 특정 사용자의 모든 댓글 조회
	List<CommentRecord> selectByBoard(@Param("boardId") String boardId);

	// 전체 댓글 조회 
	List<CommentRecord> selectAll();

	int insert(CommentRecord record);

    int update(CommentRecord record);

    int delete(@Param("commentId") String commentId);
	
}