package com.eggmoney.payv.domain.model.entity;

public enum TransactionType {
	INCOME,  // 수입 → Account.deposit()
    EXPENSE  // 지출 → Account.withdraw()
}
