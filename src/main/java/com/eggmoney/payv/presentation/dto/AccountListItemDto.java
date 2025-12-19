package com.eggmoney.payv.presentation.dto;

import lombok.Data;

/**
 * 목록 화면 전용 간략 DTO
 * @author 정의탁
 */
@Data
public class AccountListItemDto {
	private String id;
    private String name;
    private String type;       // "CASH", "BANK", "CARD" ...
    private String balance;    // Money.toString() 그대로(간단 출력용)
}
