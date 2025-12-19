package com.eggmoney.payv.presentation.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class TransactionSearchCondition {

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate start;           // inclusive	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate end;             // inclusive
	
    private String rootCategoryId;     // 선택된 상위 (옵션)
    private String categoryId;         // 선택된 하위 (옵션, 있으면 우선)
    private String accountId;          // 옵션

    // 컨트롤러 or 서비스에서 계산해 넣어 주는 필드(하위 포함 집합)
    private List<String> resolvedCategoryIds; // null이면 전체

	@Override
	public String toString() {
		return "TransactionSearchCondition [start=" + start + 
				", end=" + end + 
				", rootCategoryId=" + rootCategoryId + 
				", categoryId=" + categoryId + 
				", accountId=" + accountId + 
				", resolvedCategoryIds=" + resolvedCategoryIds + "]";
	}
       
}
