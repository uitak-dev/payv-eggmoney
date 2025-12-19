package com.eggmoney.payv.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 카테고리 셀렉트 박스용 (루트/자식 평탄화)
 * @author 정의탁
 */
@Data
@AllArgsConstructor
public class CategoryOptionDto {

	private String id;
    private String label;   // 예) "식비" / "— 편의점"
}
