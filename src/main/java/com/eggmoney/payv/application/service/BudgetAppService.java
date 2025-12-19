package com.eggmoney.payv.application.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.domain.model.entity.Budget;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.domain.model.entity.TransactionType;
import com.eggmoney.payv.domain.model.repository.BudgetRepository;
import com.eggmoney.payv.domain.model.repository.CategoryRepository;
import com.eggmoney.payv.domain.model.repository.TransactionRepository;
import com.eggmoney.payv.domain.model.vo.BudgetId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.shared.error.DomainException;

import lombok.RequiredArgsConstructor;

/**
 * 예산 애플리케이션 서비스
 * @author 정의탁
 */
@Service
@RequiredArgsConstructor
public class BudgetAppService {

	private static final ZoneId TIME_ZONE = ZoneId.of("Asia/Seoul");

	private final BudgetRepository budgetRepository;
	private final CategoryRepository categoryRepository;
	private final TransactionRepository transactionRepository;

	// 예산 등록.
	@Transactional
	public Budget createBudget(LedgerId ledgerId, CategoryId categoryId, YearMonth month, Money limit) {
		Objects.requireNonNull(ledgerId, "ledgerId");
		Objects.requireNonNull(categoryId, "categoryId");
		Objects.requireNonNull(month, "month");
		Objects.requireNonNull(limit, "limit");

		assertNotPast(month);

		// 카테고리 확인 및 가계부 일치
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new DomainException("category not found"));
		if (!category.getLedgerId().equals(ledgerId)) throw new DomainException("ledger mismatch");

		// 본인 중복 방지(Unique 보강)
		if (budgetRepository.existsFor(ledgerId, categoryId, month)) {
			throw new DomainException("해당 월에 지정된 카테고리로 등록된 예산이 이미 존재합니다.");
		}

		// 루트/자식 상호배타
		enforceMutualExclusionOnCreate(ledgerId, category, month);

		// 초기 소진액 계산(현재/미래: 미래면 0, 현재면 합산)
		Money initialSpent = sumPostedExpensesOfMonth(ledgerId, categoryId, month);

		Budget b = Budget.create(ledgerId, categoryId, month, limit);
		if (!initialSpent.isZero()) {
			b.rebaseSpentTo(initialSpent); // limit < spent 허용
		}
		budgetRepository.save(b);
		return b;
	}

	// 한도 변경.
	@Transactional
	public void changeLimit(BudgetId budgetId, Money newLimit) {
		Objects.requireNonNull(budgetId, "budgetId");
		Objects.requireNonNull(newLimit, "newLimit");

		Budget budget = budgetRepository.findById(budgetId)
				.orElseThrow(() -> new DomainException("budget not found"));

		assertNotPast(budget.getMonth());
		budget.changeLimit(newLimit);
		
		budgetRepository.save(budget);
	}
	
	// 예산 상세 조회.
	@Transactional
	public Budget getDetails(BudgetId budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new DomainException("budget not found"));
    }
	
	// 예산 상세 조회.
	@Transactional(readOnly = true)
    public Budget getDetails(LedgerId ledgerId, CategoryId categoryId, YearMonth month) {
        return budgetRepository.findOne(ledgerId, categoryId, month)
                .orElseThrow(() -> new DomainException("budget not found"));
    }
	
	// 예산 목록 조회.
	@Transactional(readOnly = true)
	public List<Budget> listByLedger(LedgerId ledgerId) {
	    return budgetRepository.findListByLedger(ledgerId);
	}
	
	/**
     * 목록: 특정 가계부의 특정 월에 설정된 '모든' 예산 반환.
     * 구현: 해당 가계부의 활성 카테고리 ID 목록을 모아, 
     * 		repository.findByCategoriesAndMonth(...) 사용.
     */
    @Transactional(readOnly = true)
    public List<Budget> listByLedgerAndMonth(LedgerId ledgerId, YearMonth month) {
        // 활성 카테고리(소프트 삭제 제외)만 가져와 ID 목록 구성
        List<CategoryId> categoryIds = categoryRepository.findListByLedger(ledgerId).stream()
                .filter(c -> !c.isDeleted())
                .map(Category::getId)
                .collect(Collectors.toList());

        List<Budget> list = budgetRepository.findByCategoriesAndMonth(ledgerId, categoryIds, month);

        // (선택) 정렬: 루트/자식의 sortOrder, name 등이 필요하면 Category를 조인해 정렬
        // 여기서는 month 동일하므로 categoryId로만 일관 정렬
        list.sort(Comparator.comparing(b -> b.getCategoryId().value()));
        return list;
    }

    /**
     * 월별 시리즈: 특정 카테고리에 대해 from ~ to(포함x) 범위의 예산을 시간순으로 반환.
     */
    @Transactional(readOnly = true)
    public List<Budget> listMonthlySeries(LedgerId ledgerId, CategoryId categoryId,
                                          YearMonth from, YearMonth to) {
        if (from.isAfter(to)) throw new IllegalArgumentException("조회기간을 올바르게 입력해주세요.");

        // 카테고리-가계부 일치 검증.
        categoryRepository.findById(categoryId).ifPresent(category -> {
            if (!category.getLedgerId().equals(ledgerId)) {
                throw new DomainException("ledger mismatch");
            }
        });

        List<Budget> result = new ArrayList<>();
        YearMonth cur = from;
        while (!cur.isAfter(to)) {
            budgetRepository.findOne(ledgerId, categoryId, cur).ifPresent(result::add);
            cur = cur.plusMonths(1);
        }
        // 시간순 정렬 보장.(루프 순서상 이미 보장되지만, 안전하게 한 번 더)
        result.sort(Comparator.comparing(Budget::getMonth));
        return result;
    }
	
    
	// ---- 내부 유틸 ----

	/** 상호배타 규칙: 
	 * - 루트면 자식 카테고리들 중에 설정된 예산이 없어야 함. 
	 * - 자식이면 부모(루트) 카테고리로 설정된 예산이 없어야 함. 
	 **/
	private void enforceMutualExclusionOnCreate(LedgerId ledgerId, Category category, YearMonth month) {
		if (category.isRoot()) {
			if (budgetRepository.existsForAnyChild(ledgerId, category.getId(), month)) {
				throw new DomainException("선택된 달(월)에 해당 카테고리의 하위 카테고리가 이미 예산에 등록되어 있습니다.");
			}
		} else {
			CategoryId parentId = category.getParentId();
			if (budgetRepository.existsFor(ledgerId, parentId, month)) {
				throw new DomainException("선택된 달(월)에 해당 카테고리의 상위 카테고리가 이미 예산에 등록되어 있습니다.");
			}
		}
	}

	// 과거 월 금지(현재/미래 허용).
	private void assertNotPast(YearMonth month) {
		YearMonth current = YearMonth.now(TIME_ZONE);
		if (month.isBefore(current)) {
			throw new DomainException("이미 지난 달(월)에 대해서 예산을 설정할 수 없습니다.");
		}
	}

	// 해당 월에 이미 게시된 지출 합계를 계산해서 초기 spent로 사용.
	private Money sumPostedExpensesOfMonth(LedgerId ledgerId, CategoryId categoryId, YearMonth month) {
		LocalDate from = month.atDay(1);
		LocalDate to = month.plusMonths(1).atDay(1); // [from, to)

		// 루트면 루트+자식, 자식이면 본인만 조회.
		List<CategoryId> categoryIds = resolveCategoryIdsForBudget(ledgerId, categoryId);

		// 존재하는 거래를 기간 조회로 불러와 서비스에서 필터링.(게시/지출/카테고리)
		List<Transaction> transactionList = transactionRepository.findByLedgerAndDateRange(ledgerId, from, to, Integer.MAX_VALUE, 0);

		long sum = transactionList.stream()
				.filter(Transaction::isPosted)
				.filter(t -> t.getType() == TransactionType.EXPENSE)
				.filter(t -> categoryIds.contains(t.getCategoryId()))
				.map(t -> t.getAmount().toLong())
				.reduce(0L, Long::sum);

		return Money.won(sum);
	}

	// 루트 카테고리면 루트+활성 자식, 자식 카테고리면 본인 id만 담은 식별자 목록 제공.
	private List<CategoryId> resolveCategoryIdsForBudget(LedgerId ledgerId, CategoryId categoryId) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new DomainException("category not found"));

		if (!category.isRoot()) {
			List<CategoryId> ids = new ArrayList<>(1);
			ids.add(category.getId());
			return ids;
		}
		
		// 루트: 활성 카테고리 목록에서 parentId == rootId 인 것만
		return categoryRepository.findListByLedger(ledgerId).stream()
				.filter(cat -> !cat.isDeleted()) // 소프트 삭제 제외(정책)
				.filter(cat -> cat.getId().equals(categoryId) || 
						(cat.getParentId() != null && cat.getParentId().equals(categoryId))) // 직계 자식
				.map(Category::getId)
				.collect(Collectors.toList());
	}
}
