package com.eggmoney.payv.presentation.api;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eggmoney.payv.application.service.CategoryAppService;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.presentation.api.CategoryApiController.SimpleCategoryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/ledgers/{ledgerId}/categories")
public class CategoryApiController {

	private final CategoryAppService categoryAppService;
	
	// 루트 카테고리의 하위(2단계) 목록 조회.
    @GetMapping(value = "/{rootId}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<SimpleCategoryDto> children(@PathVariable String ledgerId,
                                            @PathVariable String rootId) {

    	LedgerId lId = LedgerId.of(ledgerId);
        CategoryId parentId = CategoryId.of(rootId);

        List<Category> categoryList = categoryAppService.subCategoryListByLedgerAndParentCategory(lId, parentId);
        return categoryList.stream()
                .sorted(Comparator.comparingInt(Category::getSortOrder)
                		.thenComparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(c -> new SimpleCategoryDto(c.getId().toString(), c.getName()))
                .collect(Collectors.toList());
    }

    @Data 
    @AllArgsConstructor
    public static class SimpleCategoryDto {
        private String id;
        private String name;
    }
}
