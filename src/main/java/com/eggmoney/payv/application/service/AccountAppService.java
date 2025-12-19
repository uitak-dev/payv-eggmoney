package com.eggmoney.payv.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.AccountType;
import com.eggmoney.payv.domain.model.repository.AccountRepository;
import com.eggmoney.payv.domain.model.repository.LedgerRepository;
import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.shared.error.DomainException;

import lombok.RequiredArgsConstructor;

/**
 * 자산 애플리케이션 서비스
 * @author 정의탁
 */
@Service
@RequiredArgsConstructor
public class AccountAppService {

	private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    
    // 가계부 내 자산 개설
    @Transactional
    public Account createAccount(LedgerId ledgerId, AccountType type, String name, Money openingBalance) {    	
    	ensureUniqueName(ledgerId, name);
    	// 가계부 존재 검증.
        ledgerRepository.findById(ledgerId).orElseThrow(() -> new DomainException("ledger not found"));
        
        Account account = Account.create(ledgerId, type, name, openingBalance);
        accountRepository.save(account);
        return account;
    }

    // 자산 display name 변경.
    @Transactional
    public void rename(AccountId accountId, LedgerId ledgerId, String newName) {
    	ensureUniqueName(ledgerId, newName);
        Account account = loadAndCheckLedger(accountId, ledgerId);
        account.rename(newName);
        accountRepository.save(account);
    }

    // 자산 잠금.
    @Transactional
    public void archive(AccountId accountId, LedgerId ledgerId) {
        Account account = loadAndCheckLedger(accountId, ledgerId);
        account.archive();
        accountRepository.save(account);
    }

    // 자산 잠금 해제.
    @Transactional
    public void reopen(AccountId accountId, LedgerId ledgerId) {
        Account account = loadAndCheckLedger(accountId, ledgerId);
        account.reopen();
        accountRepository.save(account);
    }
    
    // 자산 (소프트)삭제.
    @Transactional
    public void delete(LedgerId ledgerId, AccountId accountId) {
    	Account account = accountRepository.findById(accountId)
    			.orElseThrow(() -> new DomainException("account not found"));
    	
    	if (!account.getLedgerId().equals(ledgerId)) {
    		throw new DomainException("ledger mismatch");
    	}
    	
    	accountRepository.delete(accountId);
    }

    // ---------- 금액 변경(SSOT: Account) ----------
    // 입금/충전/상환
    @Transactional
    public void deposit(AccountId accountId, LedgerId ledgerId, Money amount) {
        Account account = loadAndCheckLedger(accountId, ledgerId);
        account.deposit(amount);
        accountRepository.save(account);
    }

    // 출금/소비/결제
    @Transactional
    public void withdraw(AccountId accountId, LedgerId ledgerId, Money amount) {
        Account account = loadAndCheckLedger(accountId, ledgerId);
        account.withdraw(amount);
        accountRepository.save(account);
    }

    /**
     * +/- 델타 적용(정산/수정) — 자산의 현재 잔액 직접 수정.
     * 카드 외 음수 금지 규칙은 도메인이 검증.
     */
    @Transactional
    public void adjust(AccountId accountId, LedgerId ledgerId, Money delta) {
        Account account = loadAndCheckLedger(accountId, ledgerId);
        account.adjust(delta);
        accountRepository.save(account);
    }

    // ---- 조회 ----
    @Transactional(readOnly = true)
    public Account getDetails(AccountId accountId) {
        return accountRepository.findById(accountId)
        		.orElseThrow(() -> new DomainException("account not found"));
    }

    @Transactional(readOnly = true)
    public List<Account> listByLedger(LedgerId ledgerId) {
        return accountRepository.findListByLedger(ledgerId);
    }

    // ---- 내부 유틸 ----
    private Account loadAndCheckLedger(AccountId accountId, LedgerId ledgerId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException("account not found"));
        
        if (!account.getLedgerId().equals(ledgerId)) {
            throw new DomainException("account belongs to different ledger");
        }
        
        return account;
    }
    
    private void ensureUniqueName(LedgerId ledgerId, String name) {
    	accountRepository.findByLedgerAndName(ledgerId, name.trim()).ifPresent(x -> {
            throw new DomainException("동일한 자산 이름이 이미 존재합니다.");
        });
    }
}
