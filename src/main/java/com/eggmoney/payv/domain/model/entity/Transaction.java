package com.eggmoney.payv.domain.model.entity;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Function;

import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.model.vo.TransactionId;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

import lombok.Getter;

/**
 * 단일 거래(날짜/유형/금액/계정/카테고리/메모) 보관, 게시 트리거 제공.
 * 
 * 책임:
 *  - 거래 데이터 보관(날짜/유형/금액/자산/카테고리/메모/첨부)
 *  - 게시(post)/취소(unpost) 시 Account의 currentBalance를 일관성 있게 변경.
 *  - 표현(render): 표시용 문자열/요약 제공.
 * 제약:
 *  - 금액(Money)은 양수만 허용 (부호 의미는 TransactionType으로 구분)
 *  - 게시된 상태에서는 금액/계정/유형/날짜/카테고리 변경 불가(메모/첨부만 허용) → 필요 시 unpost 후 수정.
 *  - ledgerId 일치 검증: 거래의 ledgerId와 대상 Account의 ledgerId가 달라선 안 됨.
 * 
 * @author 정의탁
 */
@Getter
public class Transaction {

	private final TransactionId id;
    private final LedgerId ledgerId;
    private AccountId accountId;
    private TransactionType type;
    private LocalDate date;					// 거래 일자(사용자 설정).
    private Money amount;
    private CategoryId categoryId;
    private String memo;
    private final LocalDateTime createdAt;
    // private final List<Attachment> attachments = new ArrayList<>();

    private boolean posted;            		// 게시 여부.
    private LocalDateTime postedAt;    		// 게시 시각.
	
    private Transaction(TransactionId id, LedgerId ledgerId, AccountId accountId, 
    		TransactionType type, LocalDate date, Money amount, CategoryId categoryId, String memo,
    		boolean posted, LocalDateTime postedAt, LocalDateTime createdAt) {
    	
    	if (id == null) throw new IllegalArgumentException("id is required");
        if (ledgerId == null) throw new IllegalArgumentException("ledgerId is required");
        if (accountId == null) throw new IllegalArgumentException("accountId is required");
        if (type == null) throw new IllegalArgumentException("type is required");
        if (date == null) throw new IllegalArgumentException("date is required");
        if (amount == null || !amount.isPositive()) throw new IllegalArgumentException("amount must be positive");
        if (categoryId == null) throw new IllegalArgumentException("categoryId is required");
    	
		this.id = id;
		this.ledgerId = ledgerId;
		this.accountId = accountId;
		this.type = type;
		this.date = date;
		this.amount = amount;
		this.categoryId = categoryId;
		this.memo = (memo == null) ? "" : memo.trim();
		this.posted = posted;
		this.postedAt = postedAt;
		this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
	}
    
    // 새 거래(미게시) 생성.
    public static Transaction create(LedgerId ledgerId, AccountId accountId, 
    		TransactionType type, LocalDate date, Money amount, CategoryId categoryId, String memo) {
    	
        return new Transaction(TransactionId.of(EntityIdentifier.generateUuid()), ledgerId, accountId, 
        		type, date, amount, categoryId, memo, false, null, LocalDateTime.now());
    }

    // 인프라 복원용(레코드 → 도메인).
    public static Transaction reconstruct(TransactionId id, LedgerId ledgerId, AccountId accountId, 
    		TransactionType type, LocalDate date, Money amount, CategoryId categoryId, String memo, 
    		boolean posted, LocalDateTime postedAt, LocalDateTime createdAt) {
    	
        return new Transaction(id, ledgerId, accountId, 
        		type, date, amount, categoryId, memo, posted, postedAt, createdAt);
    }
    
    /**
     * ---- 게시/취소(SSOT: Account가 최종 잔액을 보유) ----
     */
    // 게시: Account에 반영(수입=입금, 지출=출금). idempotent 보장.
    public void post(Account account) {
        ensureSameLedger(account);
        if (posted) return; // 멱등
        if (type == TransactionType.INCOME) {
            account.deposit(amount);
        } else {
            account.withdraw(amount);
        }
        this.posted = true;
        this.postedAt = LocalDateTime.now();
    }
    
    // 게시 취소: Account에서 반대로 되돌림. idempotent 보장.
    public void unpost(Account account) {
        ensureSameLedger(account);
        if (!posted) return; // 멱등
        if (type == TransactionType.INCOME) {
            // 수입 취소 → 출금
            account.withdraw(amount);
        } else {
            // 지출 취소 → 입금
            account.deposit(amount);
        }
        this.posted = false;
        this.postedAt = null;
    }
    
    private void ensureSameLedger(Account account) {
        if (account == null) throw new IllegalArgumentException("account is required");
        if (!account.getLedgerId().equals(this.ledgerId)) {
            throw new DomainException("해당 가계부에 포함된 자산이 아닙니다.");
        }
        if (!account.getId().equals(this.accountId)) {
            throw new DomainException("해당 거래 내역을 포함하는 자산이 아닙니다.");
        }
    }
    
    /** 
     * ---- 편집 규칙 ----
     */
    private void assertNotPosted(String what){
        if (posted) throw new DomainException(what + " cannot be changed after posting. Unpost first.");
    }

    public void changeAccount(AccountId newAccountId, LedgerId ledgerId){
        assertNotPosted("account");
        if (newAccountId == null) throw new IllegalArgumentException("accountId is required");
        if (ledgerId == null) throw new IllegalArgumentException("ledgerId is required");
        if (!this.ledgerId.equals(ledgerId)) throw new DomainException("가계부 간의 거래 내역를 이동할 수 없습니다.");
        this.accountId = newAccountId;
    }

    public void changeType(TransactionType newType){
        assertNotPosted("type");
        if (newType == null) throw new IllegalArgumentException("type is required");
        this.type = newType;
    }

    public void changeDate(LocalDate newDate){
        assertNotPosted("date");
        if (newDate == null) throw new IllegalArgumentException("date is required");
        this.date = newDate;
    }

    public void changeAmount(Money newAmount){
        assertNotPosted("amount");
        if (newAmount == null || !newAmount.isPositive()) {
        	throw new IllegalArgumentException("amount must be positive");
        }
        this.amount = newAmount;
    }

    public void changeCategory(CategoryId newCategoryId){
        assertNotPosted("category");
        this.categoryId = newCategoryId; // null 허용
    }

    // 메모/첨부는 언제든 변경 가능.
    public void changeMemo(String newMemo){
        this.memo = (newMemo == null) ? "" : newMemo.trim();
    }
    
    /**
     * ---- 표현(렌더링) ----
     */
    private static final DecimalFormat KRW = new DecimalFormat("#,##0");

    // 간단 요약 문자열 (자산/카테고리 이름 해석기를 주입받아 표현력 향상)
    public String render(Function<AccountId, String> accountNameResolver,
                         Function<CategoryId, String> categoryNameResolver) {
        String sign = (type == TransactionType.INCOME) ? "+" : "-";
        String amt = KRW.format(amount.toLong()) + "원";
        String acc = (accountNameResolver != null) ? accountNameResolver.apply(accountId) : accountId.value();
        String cat = (categoryNameResolver != null && categoryId != null) ? categoryNameResolver.apply(categoryId) : categoryId.value();
        String memoPart = (memo == null || memo.isEmpty()) ? "" : (" - " + memo);
        return String.format("%s %s #%s (%s) @%s%s", date, sign + amt, cat, type, acc, memoPart);
    }
    
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction other = (Transaction) o;
        return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
