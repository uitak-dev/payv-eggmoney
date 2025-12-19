package com.eggmoney.payv.infrastructure.mybatis.record;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountRecord {

    private String accountId;
    private String ledgerId;
    private String type;				// enum: CASH/BANK/CARD/ETC
    private String name;
    private BigDecimal currentBalance;
    private String archived;			// 자산 잠금: 'Y'/'N'
    private String isDeleted;			// 'Y' / 'N'
    private LocalDateTime createdAt;
    
}
