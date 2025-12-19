package com.eggmoney.payv.infrastructure.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.eggmoney.payv.infrastructure.mybatis.record.BudgetRecord;

public interface BudgetMapper {

	BudgetRecord selectById(@Param("id") String id);

	// 해당 달(월)에 해당 카테고리로 설정된 예산 조회.
    BudgetRecord selectOne(@Param("ledgerId") String ledgerId,
                           @Param("categoryId") String categoryId,
                           @Param("yearMonth") String yearMonth);

    // 가계부에 설정된 예산 목록 조회.
    List<BudgetRecord> selectByLedger(@Param("ledgerId") String ledgerId);
    
    // 해당 달(월)에 해당 카테고리로 예산이 설정되어 있는지 확인.
    int existsFor(@Param("ledgerId") String ledgerId,
                  @Param("categoryId") String categoryId,
                  @Param("yearMonth") String yearMonth);

    // 하위 카테고리가 있는지 확인.
    int existsForAnyChild(@Param("ledgerId") String ledgerId,
                          @Param("rootId") String rootId,
                          @Param("yearMonth") String yearMonth);

    // 루트/자식 카테고리 집계 용도.
    List<BudgetRecord> selectByCategoriesAndMonth(@Param("ledgerId") String ledgerId,
                                                  @Param("categoryIds") List<String> categoryIds,
                                                  @Param("yearMonth") String yearMonth);

    int insert(BudgetRecord record);
    int update(BudgetRecord record);
    int changeCategory(@Param("id") String id, @Param("toCategoryId") String toCategoryId);
}
