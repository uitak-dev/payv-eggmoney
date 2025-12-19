package com.eggmoney.payv.application.mapper;

import org.springframework.stereotype.Component;

import com.eggmoney.payv.domain.model.entity.Board;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.presentation.dto.BoardItemDto;

/**
 * Mapper: BoardDtoMapper
 * 
 * 책임:
 * - Domain Entity(Board, User) → DTO(BoardItemDto) 변환 역할
 * - Controller/Presentation 계층에서 사용할 수 있는 데이터 전환을 담당
 * - Domain 모델을 외부로 직접 노출하지 않고 표현 계층 전용 객체로 변환
 * 
 * Layer: Application → Presentation 변환 지원
 * 
 * author 한지원
 */
@Component
public class BoardDtoMapper {
    public static BoardItemDto toDto(Board b, User owner) {
        return new BoardItemDto(
            b.getId().toString(),
            b.getUserId().toString(),
            b.getTitle(),
            b.getContent(),
            owner != null ? owner.getEmail() : "",
            b.getViewCount(),
            b.getCreatedAt(),
            b.getUpdatedAt()
        );
    }
}

