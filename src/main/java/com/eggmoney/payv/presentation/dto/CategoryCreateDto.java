package com.eggmoney.payv.presentation.dto;

import lombok.Data;

/**
 * 신규 카테고리 생성 폼 DTO
 * @author r2com
 */
@Data
public class CategoryCreateDto {
	
	// 빈 값이면 루트 생성, 값이 있으면 해당 루트의 자식 생성(2-depth 보장)
    private String parentId; 
    private String name;
}
