package com.eggmoney.payv.presentation.dto;

import lombok.Data;

/**
 * 신규 거래 등록 폼 DTO
 * @author r2com
 */
@Data
public class TransactionCreateDto {

	private String accountId;
    private String categoryId;
    private String date;          // yyyy-MM-dd
    private String type;          // TransactionType.name()
    private String amount;     	  // 정수 문자열
    private String memo;          // optional
}
