package com.eggmoney.payv.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자산 수정 폼 DTO (이름/유형 변경)
 * @author 정의탁
 */
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AccountUpdateDto {

	private String name;
    // private String type; // AccountType.name()
}
