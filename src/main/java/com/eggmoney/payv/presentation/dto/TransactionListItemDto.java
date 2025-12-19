package com.eggmoney.payv.presentation.dto;

import lombok.Data;

/**
 * 목록 표시 전용 DTO
 * @author 정의탁
 */
@Data
public class TransactionListItemDto {

	private String id;
    private String date;         // yyyy-MM-dd
    private String accountName;
    private String categoryName;
    private String type;         // INCOME/EXPENSE 등
    private String amount;       // Money 문자열
    private String memo;
}
