package com.eggmoney.payv.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 예산 한도 변경 폼 DTO
 * @author 정의탁
 */
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class BudgetUpdateDto {

	private String month;      // 리다이렉트를 위한 컨텍스트 유지.
    private String limit;      // 예산 한도 금액.
}
