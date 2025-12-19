package com.eggmoney.payv.infrastructure.mybatis.record;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LedgerRecord {

	private String ledgerId;
    private String ownerId;
    private String name;
    private LocalDateTime createdAt;
}
