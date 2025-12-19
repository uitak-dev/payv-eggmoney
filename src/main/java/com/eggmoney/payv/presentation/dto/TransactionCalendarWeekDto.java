package com.eggmoney.payv.presentation.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionCalendarWeekDto {
	
	private List<TransactionCalendarDayDto> days;
}
