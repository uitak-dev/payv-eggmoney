package com.eggmoney.payv.infrastructure.mybatis.record;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionRecord {

	private String transactionId;
	private String ledgerId; 
	private String accountId;
	private LocalDate date;			// 거래 일자.
	private String type;			// INCOME / EXPENSE
	private BigDecimal amount;
	private String categoryId;
	private String memo;

	private String posted;          // 'Y'/'N'
    private LocalDateTime postedAt;
    private LocalDateTime createdAt;
}
