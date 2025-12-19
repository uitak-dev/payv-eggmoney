package com.eggmoney.payv.domain.model.repository;

import java.util.List;
import java.util.Optional;

import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.UserId;

/**
 * 가계부 레포지토리
 * @author 정의탁
 */
public interface LedgerRepository {
	Optional<Ledger> findById(LedgerId id);
    List<Ledger> findListByOwner(UserId ownerId);
    
    // 소유자 기준 가계부 이름 중복 체크.
    boolean existsByOwnerAndName(UserId ownerId, String name);
    
    // UPSERT = 새 UUID면 insert, 아니면 update
    void save(Ledger ledger);
}
