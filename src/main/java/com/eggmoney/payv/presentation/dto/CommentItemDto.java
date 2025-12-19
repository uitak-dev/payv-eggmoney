package com.eggmoney.payv.presentation.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentItemDto {

	private String id;
    private String boardId;
    private String userId;
    private String writer;
    private String content;
    private String createdAt;
    private LocalDateTime updatedAt;
}
