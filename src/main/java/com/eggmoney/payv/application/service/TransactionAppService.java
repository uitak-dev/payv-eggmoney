package com.eggmoney.payv.application.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.Budget;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.domain.model.entity.TransactionType;
import com.eggmoney.payv.domain.model.repository.AccountRepository;
import com.eggmoney.payv.domain.model.repository.BudgetRepository;
import com.eggmoney.payv.domain.model.repository.CategoryRepository;
import com.eggmoney.payv.domain.model.repository.TransactionRepository;
import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.model.vo.TransactionId;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.presentation.dto.PageRequestDto;
import com.eggmoney.payv.presentation.dto.PageResultDto;
import com.eggmoney.payv.presentation.dto.TransactionSearchCondition;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionAppService {

	private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    
    // 생성.
    @Transactional
    public Transaction create(LedgerId ledgerId, AccountId accountId,
                              TransactionType type, LocalDate date, Money amount,
                              CategoryId categoryId, String memo) {

        // 자산 검증.
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException("account not found"));
        if (!account.getLedgerId().equals(ledgerId)) {
            throw new DomainException("해당 자산은 현재 가계부에 포함되어 있지 않습니다.");
        }

        // 카테고리 검증 (필수 + 동일 가계부)
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new DomainException("관련 카테고리를 찾을 수 없습니다."));
        if (!category.getLedgerId().equals(ledgerId)) {
            throw new DomainException("해당 가계부에 속한 카테고리가 아닙니다.");
        }

        Transaction transaction = Transaction.create(ledgerId, accountId, type, date, amount, categoryId, memo);
        transactionRepository.save(transaction);
        return transaction;
    }

    // transaction(거래 내역) 게시.
    @Transactional
    public void post(TransactionId transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
        		.orElseThrow(() -> new DomainException("transaction not found"));
        
        Account account = accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new DomainException("account not found"));
        
        // 도메인 규칙에 따라 자산/가계부 일치 여부는 Transaction.post() 내부에서 재검증.
        transaction.post(account);
        applyBudgetOnPost(transaction);
        
        transactionRepository.save(transaction);
        accountRepository.save(account); // 잔액 SSOT 반영
    }
    
    // transaction(거래 내역) 취소.
    @Transactional
    public void unpost(TransactionId transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
        		.orElseThrow(() -> new DomainException("transaction not found"));
        
        Account account = accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new DomainException("account not found"));
        
        transaction.unpost(account);
        applyBudgetOnUnpost(transaction);
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
    }
    
    // 편집 (게시 전만)
    @Transactional
    public void updateDetails(TransactionId transactionId, AccountId newAccountId, TransactionType newType,
                              LocalDate newDate, Money newAmount, CategoryId newCategoryId, String newMemo) {

        Transaction transaction = transactionRepository.findById(transactionId)
        		.orElseThrow(() -> new DomainException("transaction not found"));

        if (transaction.isPosted()) {
            throw new DomainException("게시된 거래 내역을 수정할 수 없습니다. 게시 취소를 먼저 해주세요.");
        }
        
        // 자산 이동 시, 가계부(Ledger) 일치 검증.
        if (newAccountId != null && !newAccountId.equals(transaction.getAccountId())) {
            
        	Account newAccount = accountRepository.findById(newAccountId)
                    .orElseThrow(() -> new DomainException("account not found"));
            
            if (!newAccount.getLedgerId().equals(transaction.getLedgerId())) {
                throw new DomainException("거래 내역을 다른 가계부로 이동할 수 없습니다.");
            }
            transaction.changeAccount(newAccountId, transaction.getLedgerId());
        }

        if (newType != null && newType != transaction.getType()) {
        	transaction.changeType(newType);
        }
        if (newDate != null && !newDate.equals(transaction.getDate())) {
        	transaction.changeDate(newDate);
        }
        if (newAmount != null && !newAmount.equals(transaction.getAmount())) {
        	transaction.changeAmount(newAmount);
        }

        if (newCategoryId != null && !newCategoryId.equals(transaction.getCategoryId())) {
            Category newCategory = categoryRepository.findById(newCategoryId)
                    .orElseThrow(() -> new DomainException("category not found"));
            
            if (!newCategory.getLedgerId().equals(transaction.getLedgerId())) {
                throw new DomainException("해당 가계부에 속한 카테고리가 아닙니다.");
            }
            
            transaction.changeCategory(newCategoryId);
        }

        if (newMemo != null) {
        	transaction.changeMemo(newMemo);
        }

        transactionRepository.save(transaction);
    }
    
    // 삭제 (게시되지 않은 경우에만)
    @Transactional
    public void delete(TransactionId transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
        		.orElseThrow(() -> new DomainException("transaction not found"));
        
        if (transaction.isPosted()) {
            throw new DomainException("게시된 거래 내역을 삭제할 수 없습니다. 게시 취소를 먼저 해주세요.");
        }
        
        transactionRepository.delete(transactionId);
    }
    
    // 원클릭 게시: 거래 내역 생성과 게시까지 한 트랜잭션에서 처리.
 	@Transactional
 	public Transaction oneClickCreate(LedgerId ledgerId, AccountId accountId, TransactionType type, LocalDate date,
 			Money amount, CategoryId categoryId, String memo) {
 		
 		// 자산 검증.
 		Account account = accountRepository.findById(accountId)
 				.orElseThrow(() -> new DomainException("account not found"));
 		if (!account.getLedgerId().equals(ledgerId))
 			throw new DomainException("account belongs to different ledger");

 		// 카테고리 검증 (필수 + 동일 가계부)
 		Category category = categoryRepository.findById(categoryId)
 				.orElseThrow(() -> new DomainException("category not found"));
 		
 		if (!category.getLedgerId().equals(ledgerId)) {
 			throw new DomainException("category belongs to different ledger");
 		}

 		// 초안 생성 후, 곧바로 게시.
 		Transaction transaction = Transaction.create(ledgerId, accountId, type, date, amount, categoryId, memo);

 		// Account 잔액 반영 (지출이면 withdraw, 수입이면 deposit).
 		transaction.post(account);
 		applyBudgetOnPost(transaction);	// 예산 반영.
 		transactionRepository.save(transaction); // 게시 상태로 저장.
 		accountRepository.save(account); // 변경된 잔액 저장.
 		
 		return transaction;
 	}
    
 	// 원클릭 수정: (게시되어 있으면) unpost → 변경 적용 → post 까지 한 트랜잭션에서 처리.
 	@Transactional
 	public Transaction oneClickUpdate(TransactionId transactionId, AccountId newAccountId,
 	                                   TransactionType newType, LocalDate newDate, Money newAmount, 
 	                                   CategoryId newCategoryId, String newMemo) {

 		Objects.requireNonNull(transactionId, "transactionId");
 	    Objects.requireNonNull(newAccountId, "newAccountId");
 	    Objects.requireNonNull(newType, "newType");
 	    Objects.requireNonNull(newDate, "newDate");
 	    Objects.requireNonNull(newAmount, "newAmount");
 	    Objects.requireNonNull(newCategoryId, "newCategoryId");
 	    Objects.requireNonNull(newMemo, "newMemo");
 		
 	    Transaction transaction = transactionRepository.findById(transactionId)
 	    		.orElseThrow(() -> new DomainException("tx not found"));

 	    // 기존 자산/가계부 조회.
 	    Account oldAccount = accountRepository.findById(transaction.getAccountId())
 	            .orElseThrow(() -> new DomainException("account not found"));

 	    // 대상 자산/카테고리 존재/동일 가계부 검증
 	    LedgerId ledgerId = transaction.getLedgerId();
 	    
 	    // 자산이 변경된 경우, 대상 자산 로딩 + 동일 가계부 검증.
 	    Account targetAccount = oldAccount;
 	    if (!newAccountId.equals(oldAccount.getId())) {
 	        targetAccount = accountRepository.findById(newAccountId)
 	                .orElseThrow(() -> new DomainException("new account not found"));
 	        if (!targetAccount.getLedgerId().equals(ledgerId)) {
 	            throw new DomainException("account ledger mismatch");
 	        }
 	    }

 	    // 카테고리가 변경된 경우, 검증.
 	    if (!newCategoryId.equals(transaction.getCategoryId())) {
 	        Category category = categoryRepository.findById(newCategoryId)
 	                .orElseThrow(() -> new DomainException("category not found"));
 	        if (!category.getLedgerId().equals(ledgerId)) {
 	            throw new DomainException("category ledger mismatch");
 	        }
 	    }

 	    // 게시되어 있었다면 먼저 원복.( 멱등 )
 	    if (transaction.isPosted()) {
 	    	transaction.unpost(oldAccount);		// 기존 자산 잔액 되돌림.
 	    	applyBudgetOnUnpost(transaction);	// 기존 예산 잔액 되돌림.
 	        accountRepository.save(oldAccount);	// SSOT 저장.
 	    }

 	    // 수정.(Transaction은 '미게시일 때만' 변경 허용)
		transaction.changeAccount(newAccountId, ledgerId);
		transaction.changeType(newType);
		transaction.changeDate(newDate);
		transaction.changeAmount(newAmount);
		transaction.changeCategory(newCategoryId);
		transaction.changeMemo(newMemo);

 	    // 재게시(repost).
        transaction.post(targetAccount);
        applyBudgetOnPost(transaction);
        accountRepository.save(targetAccount);
        transactionRepository.save(transaction);
 	    
        return transaction;
 	}

 	// 원클릭 삭제: (게시되어 있으면) unpost → delete 를 한 트랜잭션에서 처리.
	@Transactional
	public void oneClickDelete(TransactionId transactionId) {
		Transaction transaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new DomainException("transaction not found"));

		Account account = accountRepository.findById(transaction.getAccountId())
				.orElseThrow(() -> new DomainException("account not found"));

		// 게시되어 있었다면 먼저 원복.( 멱등 )
		if (transaction.isPosted()) {
			transaction.unpost(account); 		// 기존 자산 잔액 되돌림.
			applyBudgetOnUnpost(transaction);	// 기존 예산 잔액 되돌림.
			accountRepository.save(account);
		}
		
		transactionRepository.delete(transactionId);
	}
	
	/**
	 * <예산 연동 유틸>
	 * - 거래 내역(Transaction) 게시(post) 시, 
	 * - 자산(Account)에 지출 내역 반영 책임은 도메인 객체(엔티티)에 할당. 반면에, 
	 * - 예산(Budget)에 지출 내역 반영 책임은 애플리케이션 서비스 객체에서 수행.
	 */
	private void applyBudgetOnPost(Transaction transaction) {
        if (transaction.getType() != TransactionType.EXPENSE) return;

        YearMonth ym = YearMonth.from(transaction.getDate());
        Budget budget = resolveBudgetFor(transaction.getLedgerId(), transaction.getCategoryId(), ym);
        if (budget != null) {
            budget.registerExpense(transaction.getAmount());
            budgetRepository.save(budget);
        }
    }

    private void applyBudgetOnUnpost(Transaction transaction) {
        if (transaction.getType() != TransactionType.EXPENSE) return;

        YearMonth ym = YearMonth.from(transaction.getDate());
        Budget budget = resolveBudgetFor(transaction.getLedgerId(), transaction.getCategoryId(), ym);
        if (budget != null) {
            budget.releaseExpense(transaction.getAmount());
            budgetRepository.save(budget);
        }
    }

    /**
     * 해당 거래의 월/카테고리에 대응하는 예산을 찾습니다.
     * - 정확히 일치하는 카테고리 예산 우선 찾음.
     * - 없다면, 해당 카테고리가 자식이면 부모(루트) 예산을 대체로 사용.
     * - 그래도 없으면, null (예산 미설정)
     */
    private Budget resolveBudgetFor(LedgerId ledgerId, CategoryId categoryId, YearMonth ym) {
        return budgetRepository.findOne(ledgerId, categoryId, ym)
                .orElseGet(() -> {
                    // 부모(루트) 확인.
                    Category cat = categoryRepository.findById(categoryId).orElse(null);
                    if (cat == null || cat.isRoot()) return null;
                    return budgetRepository.findOne(ledgerId, cat.getParentId(), ym).orElse(null);
                });
    }

    // ---- 조회 ----
    @Transactional(readOnly = true)
    public List<Transaction> listByLedgerAndPeriod(LedgerId ledgerId, LocalDate from, LocalDate to,
                                                   int limit, int offset) {
        return transactionRepository.findByLedgerAndDateRange(ledgerId, from, to, limit, offset);
    }
    
    
    
    @Transactional(readOnly = true)
    public PageResultDto<Transaction> search(LedgerId ledgerId, TransactionSearchCondition cond, PageRequestDto page) {
    	return transactionRepository.search(ledgerId, cond, page);
    }
    
    

    @Transactional(readOnly = true)
    public Transaction getDetails(TransactionId transactionId) {
        return transactionRepository.findById(transactionId)
        		.orElseThrow(() -> new DomainException("transaction not found"));
    }
    
	// 월별 조회.
	@Transactional(readOnly = true)
	public List<Transaction> listByMonth(LedgerId ledgerId, YearMonth month, int limit, int offset) {
		return transactionRepository.findByLedgerAndMonth(ledgerId, month, limit, offset);
	}
	
	// 자산별 조회.
	@Transactional(readOnly = true)
	public List<Transaction> listByAccount(LedgerId ledgerId, AccountId accountId, int limit, int offset) {
		// 자산 검증.
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException("account not found"));
        if (!account.getLedgerId().equals(ledgerId)) {
            throw new DomainException("해당 자산은 현재 가계부에 포함되어 있지 않습니다.");
        }
        
		return transactionRepository.findByLedgerAndAccount(ledgerId, accountId, limit, offset);
	}

	// 카테고리별 조회( 상위 선택 시 하위 포함 ).
	@Transactional(readOnly = true)
	public List<Transaction> listByCategory(LedgerId ledgerId, CategoryId categoryId, boolean includeChildren,
			int limit, int offset) {
		
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new DomainException("category not found"));
		if (!category.getLedgerId().equals(ledgerId)) {
			throw new DomainException("해당 카테고리는 현재 가계부에 포함되어 있지 않습니다.");
		}

		List<CategoryId> ids = new ArrayList<>();
		ids.add(categoryId);

		if (includeChildren && category.isRoot()) {
			// 활성 카테고리만 포함(정책).
			ids.addAll(categoryRepository.findListByLedger(ledgerId).stream()
					.filter(c -> c.getParentId() != null && c.getParentId().equals(categoryId))
					.map(Category::getId)
					.collect(Collectors.toList()));
		}

		return transactionRepository.findByLedgerAndCategoryIds(ledgerId, ids, limit, offset);
	}
	
}
