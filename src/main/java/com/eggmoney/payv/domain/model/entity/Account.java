package com.eggmoney.payv.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Aggregate: Account
 * - 책임: 자산 상태 관리, currentBalance(Money)의 단일 진실 유지.
 * - 외부는 deposit(), withdraw(), adjust() 으로만 잔액을 변경.
 * @author 정의탁
 */
@Getter
public class Account {

	private final AccountId id;
    private final LedgerId ledgerId;
    private final AccountType type;
    private String name;
    private Money currentBalance;
    
    // 자산 잠금 (true: 거래 불가).
    private boolean archived;
    
    private boolean isDeleted;
    private final LocalDateTime createdAt;
    
	private Account(AccountId id, LedgerId ledgerId, AccountType type, 
			String name, Money currentBalance, boolean archived, boolean isDeleted,LocalDateTime createdAt) {
		if (id == null)
			throw new IllegalArgumentException("id is required");
		if (ledgerId == null)
			throw new IllegalArgumentException("ledgerId is required");
		if (type == null)
			throw new IllegalArgumentException("type is required");
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("name is required");

		this.id = id;
		this.ledgerId = ledgerId;
		this.type = type;
		this.name = name.trim();
		this.currentBalance = currentBalance == null ? Money.zero() : currentBalance;
		if (!allowsNegative(type) && this.currentBalance.isNegative()) {
			throw new DomainException("해당 자산 유형은 마이너스 값을 가질 수 없습니다: " + type);
		}
		this.archived = archived;
		this.isDeleted = isDeleted;
		this.createdAt = createdAt;
	}
	
	public static Account create(LedgerId ledgerId, AccountType type, String name, Money currentBalance) {
        return new Account(AccountId.of(EntityIdentifier.generateUuid()), ledgerId, type, 
        		name, currentBalance, false, false, LocalDateTime.now());
    }
	
	// 인프라 복원용 (레코드 → 도메인)
    public static Account reconstruct(AccountId id, LedgerId ledgerId, AccountType type, String name,
                                      Money currentBalance, boolean archived, boolean isDeleted, LocalDateTime createdAt) {
        return new Account(id, ledgerId, type, name, currentBalance, archived, isDeleted, createdAt);
    }
    
    /**
     * ----- 도메인 책임 (SSOT) -----
     */
    // 잔액 증가
    public void deposit(Money amount) {
        ensureActive();
        requirePositive(amount);
        this.currentBalance = this.currentBalance.plus(amount);
    }

    // 잔액 감소
    public void withdraw(Money amount) {
        ensureActive();
        requirePositive(amount);
        Money next = this.currentBalance.minus(amount);
        if (!allowsNegative(this.type) && next.isNegative()) {
            throw new DomainException("insufficient balance for " + type);
        }
        this.currentBalance = next;
    }

    /**
     * +/- 델타 직접 적용(정산/조정용).
     * 규칙은 withdraw(), deposit()와 동일.
     */
    public void adjust(Money delta) {
        ensureActive();
        Money next = this.currentBalance.plus(delta);
        if (!allowsNegative(this.type) && next.isNegative()) {
            throw new DomainException("adjustment would make negative balance for " + type);
        }
        this.currentBalance = next;
    }
    
    // 자산 이름 변경.
    public void rename(String newName) {
        if (newName == null || newName.trim().isEmpty()) 
        	throw new IllegalArgumentException("name is required");
        this.name = newName.trim();
    }

    public void archive() { this.archived = true; }
    public void reopen()  { this.archived = false; }
    public void delete() { this.isDeleted = true; }

    /**
     * ---- 내부 규칙/유틸 ----
     */
	private void ensureActive() {
		if (archived) throw new DomainException("해당 자산은 잠겨 있습니다.(거래 불가)");
	}

	// 카드와 기타(etc)는 음수 허용(신용 사용)
	private static boolean allowsNegative(AccountType type) {
		return type == AccountType.CARD || type == AccountType.ETC;
	}

	private static void requirePositive(Money money) {
		if (money == null || !money.isPositive())
			throw new IllegalArgumentException("금액은 양수이어야 합니다.");
	}
    
	@Override 
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account other = (Account) o;
        return id != null && id.equals(other.getId());
    }

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
