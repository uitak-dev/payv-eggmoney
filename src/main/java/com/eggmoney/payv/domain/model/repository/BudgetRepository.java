package com.eggmoney.payv.domain.model.repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.eggmoney.payv.domain.model.entity.Budget;
import com.eggmoney.payv.domain.model.vo.BudgetId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;

public interface BudgetRepository {

	Optional<Budget> findById(BudgetId id);
    Optional<Budget> findOne(LedgerId ledgerId, CategoryId categoryId, YearMonth month);
    List<Budget> findListByLedger(LedgerId ledgerId);
    
    // 해당 카테고리로 설정한 예산 존재 여부 확인.
    boolean existsFor(LedgerId ledgerId, CategoryId categoryId, YearMonth month);
    
    // 루트 카테고리의 자식 중 예산으로 설정한 카테고리 존재 여부 확인.
    boolean existsForAnyChild(LedgerId ledgerId, CategoryId rootId, YearMonth month);

    void save(Budget budget);

    // (읽기용) 루트/자식 집계에서 사용할 수 있는 API.
    List<Budget> findByCategoriesAndMonth(LedgerId ledgerId, List<CategoryId> categoryIds, YearMonth month);
}
