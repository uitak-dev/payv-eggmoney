package com.eggmoney.payv.infrastructure.mybatis.repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.eggmoney.payv.domain.model.entity.Budget;
import com.eggmoney.payv.domain.model.repository.BudgetRepository;
import com.eggmoney.payv.domain.model.vo.BudgetId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.infrastructure.mybatis.mapper.BudgetMapper;
import com.eggmoney.payv.infrastructure.mybatis.record.BudgetRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyBatisBudgetRepository implements BudgetRepository {

	private final BudgetMapper mapper;
	
	@Override
    public Optional<Budget> findById(BudgetId id) {
        BudgetRecord budgetRecord = mapper.selectById(id.value());
        return Optional.ofNullable(budgetRecord).map(this::toDomain);
    }

    @Override
    public Optional<Budget> findOne(LedgerId ledgerId, CategoryId categoryId, YearMonth month) {
        BudgetRecord budgetRecord = mapper.selectOne(ledgerId.value(), categoryId.value(), month.toString());
        return Optional.ofNullable(budgetRecord).map(this::toDomain);
    }
    
    @Override
    public List<Budget> findListByLedger(LedgerId ledgerId) {
        return mapper.selectByLedger(ledgerId.value())
        		.stream()
        		.map(this::toDomain)
        		.collect(Collectors.toList());
    }

    @Override
    public boolean existsFor(LedgerId ledgerId, CategoryId categoryId, YearMonth month) {
        return mapper.existsFor(ledgerId.value(), categoryId.value(), month.toString()) > 0;
    }

    // 루트 카테고리의 하위 카테고리 중 월 예산 존재 여부.(깊이=2)
    @Override
    public boolean existsForAnyChild(LedgerId ledgerId, CategoryId rootId, YearMonth month) {
        return mapper.existsForAnyChild(ledgerId.value(), rootId.value(), month.toString()) > 0;
    }

    @Override
    public void save(Budget budget) {
        BudgetRecord record = toRecord(budget);
        BudgetRecord existing = mapper.selectById(budget.getId().value());
        try {
            if (existing == null) mapper.insert(record);
            else mapper.update(record);
        } catch (DuplicateKeyException dup) {
            // Unique(LEDGER_ID, CATEGORY_ID, YEAR_MONTH) 위반 등 → 도메인 예외로 변환.
            throw new DomainException("budget already exists for this category and month", dup);
        }
    }

    @Override
    public List<Budget> findByCategoriesAndMonth(LedgerId ledgerId, List<CategoryId> categoryIds, YearMonth month) {
        List<String> ids = categoryIds.stream()
        		.map(CategoryId::value)
        		.collect(Collectors.toList());
        
        return mapper.selectByCategoriesAndMonth(ledgerId.value(), ids, month.toString())
        		.stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ---- 변환 ----
    private Budget toDomain(BudgetRecord record) {
    	LocalDateTime created = (record.getCreatedAt() == null) ? null : record.getCreatedAt().toLocalDateTime();
        return Budget.reconstruct(
                BudgetId.of(record.getBudgetId()),
                LedgerId.of(record.getLedgerId()),
                CategoryId.of(record.getCategoryId()),
                YearMonth.parse(record.getYearMonth()),		// 'YYYY-MM'
                Money.won(record.getLimitAmount()),
                Money.won(record.getSpentAmount()),
                created
        );
    }

    private BudgetRecord toRecord(Budget budget) {
        return BudgetRecord.builder()
        		.budgetId(budget.getId().value())
				.ledgerId(budget.getLedgerId().value())
				.categoryId(budget.getCategoryId().value())
				.yearMonth(budget.getMonth().toString())	// 'YYYY-MM'
				.limitAmount(budget.getLimit().toLong())
				.spentAmount(budget.getSpent().toLong())
				.build();
    }
}
