package com.eggmoney.payv.presentation.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: BoardItemDto
 * 
 * 책임:
 * - Board 데이터를 Presentation 계층에서 안전하게 전달하기 위한 객체
 * - Domain Entity(Board)를 직접 노출하지 않고 화면/컨트롤러 전용 데이터 구조 제공
 * 
 * Layer: Presentation DTO
 * 
 * author 한지원
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardItemDto {

	private String id;
	private String userId;
	private String title;
	private String content;
	private String owner;	// 작성자 이메일
	private long viewCount;
	private LocalDateTime createdAt;
    private LocalDateTime updatedAt;	
}
