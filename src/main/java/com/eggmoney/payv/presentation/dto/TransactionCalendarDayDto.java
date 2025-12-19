package com.eggmoney.payv.presentation.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class TransactionCalendarDayDto {

	private String date;        // yyyy-MM-dd
    private boolean inMonth;    // 해당 달에 속하는 날인지 확인.
    private String income;      // 일자 수입 합계 (Money 문자열)
    private String expense;     // 일자 지출 합계 (Money 문자열)
    private List<TxnMiniDto> txns = new ArrayList<>(); // 셀에 보여줄 간단 목록(최대 N)

    @Data
    @AllArgsConstructor
    public static class TxnMiniDto {
        private String id;
        private String categoryName;
        private String amount;     // 금액 표시.
        private String type;       // INCOME / EXPENSE
    }
}