package com.eggmoney.payv.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageInfo {
    private int page;       // 현재 페이지
    private int totalPage;  // 전체 페이지 수
    private int startPage;  // 현재 블럭 시작 페이지
    private int endPage;    // 현재 블럭 끝 페이지
    private boolean hasPrev; // 이전 블럭 존재 여부
    private boolean hasNext; // 다음 블럭 존재 여부
}
