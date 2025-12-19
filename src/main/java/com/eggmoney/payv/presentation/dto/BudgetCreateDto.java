package com.eggmoney.payv.presentation.dto;

import lombok.Data;

/**
 * 신규 예산 생성 폼 DTO
 * @author 정의탁
 */
@Data
public class BudgetCreateDto {

	private String month;        // "yyyy-MM"
    private String categoryId;   // 대상 카테고리
    private String limit;     	 // 예산 한도 금액.
}
