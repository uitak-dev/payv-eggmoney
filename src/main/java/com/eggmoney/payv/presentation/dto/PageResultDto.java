package com.eggmoney.payv.presentation.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResultDto<T> {

	private long total;
    private int page;
    private int size;
    private List<T> content;

    public int totalPages(){
        if (size <= 0) return 1;
        long tp = (total + size - 1) / size;
        return (int)(tp == 0 ? 1 : tp);
    }
}
