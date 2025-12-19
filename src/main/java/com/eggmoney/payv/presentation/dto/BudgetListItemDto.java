package com.eggmoney.payv.presentation.dto;

import lombok.Data;

/**
 * 목록 표시용 DTO.
 * @author 정의탁
 */
@Data
public class BudgetListItemDto {

	private String id;
    private String categoryId;
    private String categoryName;
    private String month;        // yyyy-MM
    private String limit;        // 예산 한도 금액(Money 문자열)

    private String spent;        // 해당 항목으로 소비한 금액.
}
