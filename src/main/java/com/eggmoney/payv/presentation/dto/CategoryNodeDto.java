package com.eggmoney.payv.presentation.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 카테고리 DTO
 * @author r2com
 */
@Data
public class CategoryNodeDto {

	private String id;
    private String name;
    private String parentId; // null이면 루트
    private boolean system;
    private int sortOrder;
    private List<CategoryNodeDto> children = new ArrayList<>();
}
