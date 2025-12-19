package com.eggmoney.payv.domain.model.repository;

import java.util.List;
import java.util.Optional;

import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;

/**
 * 자산 레포지토리
 * @author 정의탁
 */
public interface AccountRepository {

	Optional<Account> findById(AccountId id);
	List<Account> findListByLedger(LedgerId ledgerId);
	
	// 한 가계부에 동일한 이름을 갖는 자산이 존재하는지 확인.
	Optional<Account> findByLedgerAndName(LedgerId ledgerId, String name);
	
	// UPSERT = 새로 생성된 자산은 insert, 기존이면 update.
    void save(Account account);
    
    // 소프트 삭제.
    void delete(AccountId id);
}
