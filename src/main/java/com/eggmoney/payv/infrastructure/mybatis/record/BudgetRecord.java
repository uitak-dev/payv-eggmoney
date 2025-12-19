package com.eggmoney.payv.infrastructure.mybatis.record;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetRecord {

	private String budgetId;
    private String ledgerId;
    private String categoryId;
    private String yearMonth;    // 'YYYY-MM'
    private Long limitAmount;
    private Long spentAmount;
    private Timestamp createdAt;
}
