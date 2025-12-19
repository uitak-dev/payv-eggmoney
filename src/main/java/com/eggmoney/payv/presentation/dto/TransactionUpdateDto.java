package com.eggmoney.payv.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 거래 수정 폼 DTO
 * @author 정의탁
 */
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class TransactionUpdateDto {

	private String accountId;
    private String categoryId;
    private String date;       // yyyy-MM-dd
    private String type;       // TransactionType.name()
    private String amount;     // 정수 문자열
    private String memo;
}
