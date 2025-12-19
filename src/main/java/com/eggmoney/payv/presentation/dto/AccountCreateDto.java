package com.eggmoney.payv.presentation.dto;

import lombok.Data;

/**
 * 신규 자산 생성 폼 DTO 
 * @author 정의탁
 */
@Data
public class AccountCreateDto {
	
	private String name;
    private String type;           // AccountType.name()
    private String openingBalanceWon; // "0" 또는 정수 문자열 (옵션)
}
