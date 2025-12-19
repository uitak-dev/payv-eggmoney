package com.eggmoney.payv.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {

	private int page = 1;   // 1-based
    private int size = 20;  // page size

	public int offset() {
		return Math.max(0, (page <= 1 ? 0 : (page - 1) * size));
	}

	public int limit() {
		return size <= 0 ? 20 : size;
	}

	@Override
	public String toString() {
		return "PageRequestDto [page=" + page + 
				", size=" + size + "]";
	}
}
