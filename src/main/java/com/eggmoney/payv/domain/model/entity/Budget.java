package com.eggmoney.payv.domain.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

import com.eggmoney.payv.domain.model.vo.BudgetId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

import lombok.Getter;

/**
 * 월 × 카테고리 × 가계부 예산 한도/소진 상태 관리 
 * - limit: 해당 월의 예산 한도 
 * - spent: 해당 월에 소진된 금액(지출만 반영)
 *
 * 규칙: 
 * - limit, spent 는 음수 불가. 
 * - limit 은 spent 보다 작을 수 없음.(한도 축소 시) 
 * - 지출 등록은 spent += amount, 취소는 spent -= amount (0 미만 불가)
 */
@Getter
public class Budget {

	private final BudgetId id;
	private final LedgerId ledgerId;
	private final CategoryId categoryId;
	private final YearMonth month;

	private Money limit; 	// 예산 한도 (>= 0)
	private Money spent; 	// 소진 금액 (>= 0)
	private final LocalDateTime createdAt;

	// ---- 생성/복원 ----
	private Budget(BudgetId id, LedgerId ledgerId, CategoryId categoryId, 
			YearMonth month, Money limit, Money spent, LocalDateTime createdAt) {
		
		this.id = Objects.requireNonNull(id, "id");
		this.ledgerId = Objects.requireNonNull(ledgerId, "ledgerId");
		this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
		this.month = Objects.requireNonNull(month, "month");
		this.limit = requireNonNegative(limit, "limit");
		this.spent = requireNonNegative(spent, "spent");	// 초과 허용: limit < spent 가능
		
//		if (lt(this.limit, this.spent)) {
//			throw new DomainException("limit cannot be less than spent");
//		}
		
		this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
	}

	// 새 예산 생성.(spent = 0)
	public static Budget create(LedgerId ledgerId, CategoryId categoryId, YearMonth month, Money limit) {
		return new Budget(BudgetId.of(EntityIdentifier.generateUuid()), ledgerId, categoryId, 
				month, limit, Money.won(0), LocalDateTime.now());
	}

	// 인프라 복원용.(레코드 → 도메인)
	public static Budget reconstruct(BudgetId id, LedgerId ledgerId, CategoryId categoryId, YearMonth month,
			Money limit, Money spent, LocalDateTime createdAt) {
		return new Budget(id, ledgerId, categoryId, month, limit, spent, createdAt);
	}

	
	/**
     * ----- 도메인 책임 (SSOT) -----
     */	

	// 현재 한도 변경: spent 보다 작아도 허용 - 초과 상태는 표시로 관리.
	public void changeLimit(Money newLimit) {
		this.limit = requireNonNegative(newLimit, "limit");
	}

	// 지출 반영(게시(post) 시 호출): spent += amount(>0)
	public void registerExpense(Money amount) {
		amount = requirePositive(amount, "amount");
		this.spent = this.spent.plus(amount);
	}

	// 지출 반영 취소(게시 취소(unpost) 시 호출): spent -= amount(>0, spent 이상은 불가)
	public void releaseExpense(Money amount) {
		amount = requirePositive(amount, "amount");
		if (gt(amount, this.spent)) {
			throw new DomainException("cannot release more than spent");
		}
		this.spent = this.spent.minus(amount);
	}
	
	// 초기 소진액으로 재기준(서비스에서 현재 월 생성 시 사용).
    public void rebaseSpentTo(Money initialSpent) {
        this.spent = requireNonNegative(initialSpent, "initialSpent");
    }

	// 해당 날짜가 이 예산의 month에 속하는지 확인.
	public boolean contains(LocalDate date) {
		return YearMonth.from(date).equals(this.month);
	}

	// 남은 예산( 음수면 초과 )
	public Money remaining() {
		BigDecimal rest = limit.toBigDecimal().subtract(spent.toBigDecimal());
		return Money.of(rest);
	}

	// 초과 여부.
	public boolean isExceeded() {
		return lt(this.limit, this.spent);
	}

	// ---- 내부 유틸(돈 비교/검증) ----
	private static Money requireNonNegative(Money money, String what) {
		if (money == null) throw new IllegalArgumentException(what + " is required");
		if (money.toBigDecimal().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException(what + " must be >= 0");
		}
		return money;
	}

	private static Money requirePositive(Money money, String what) {
		if (money == null) throw new IllegalArgumentException(what + " is required");
		if (!money.isPositive()) {
			throw new IllegalArgumentException(what + " must be positive(> 0)");
		}
		return money;
	}

	private static boolean lt(Money a, Money b) {
		return a.toBigDecimal().compareTo(b.toBigDecimal()) < 0;
	}

	private static boolean gt(Money a, Money b) {
		return a.toBigDecimal().compareTo(b.toBigDecimal()) > 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Budget)) return false;
		return id != null && id.equals(((Budget) o).getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}