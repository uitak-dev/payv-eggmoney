package com.eggmoney.payv.infrastructure.mybatis.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.AccountType;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.repository.AccountRepository;
import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.infrastructure.mybatis.mapper.AccountMapper;
import com.eggmoney.payv.infrastructure.mybatis.record.AccountRecord;
import com.eggmoney.payv.infrastructure.mybatis.record.CategoryRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyBatisAccountRepository implements AccountRepository {

	private final AccountMapper mapper;
	
	@Override
    public Optional<Account> findById(AccountId id) {
        AccountRecord r = mapper.selectById(id.value());
        return Optional.ofNullable(r).map(this::toDomain);
    }
	
	@Override
    public Optional<Account> findByLedgerAndName(LedgerId ledgerId, String name) {
		AccountRecord accountRecord = mapper.selectByLedgerAndName(ledgerId.value(), name);
        return Optional.ofNullable(accountRecord).map(this::toDomain);
    }
	
	@Override
    public List<Account> findListByLedger(LedgerId ledgerId) {
        return mapper.selectListByLedger(ledgerId.value())
                     .stream()
                     .map(this::toDomain)
                     .collect(Collectors.toList());
    }

    @Override
    public void save(Account account) {
        AccountRecord rec = toRecord(account);
        AccountRecord existing = mapper.selectById(account.getId().value());
        if (existing == null) {
            mapper.insert(rec);
        } else {
            mapper.update(rec);
        }
    }
    
    @Override
    public void delete(AccountId id) {
        mapper.delete(id.value());
    }

    // ---------- 변환부 ----------
    private Account toDomain(AccountRecord record) {
        return Account.reconstruct(
                AccountId.of(record.getAccountId()),
                LedgerId.of(record.getLedgerId()),
                AccountType.valueOf(record.getType()),
                record.getName(),
                Money.of(record.getCurrentBalance()),	// KRW scale=0
                "Y".equals(record.getArchived()),
                "Y".equals(record.getIsDeleted()),
                record.getCreatedAt() != null ? record.getCreatedAt() : LocalDateTime.now()
        );
    }

    private AccountRecord toRecord(Account account) {        
        return AccountRecord.builder()
        		.accountId(account.getId().value())
        		.ledgerId(account.getLedgerId().value())
        		.type(account.getType().name())
        		.name(account.getName())
        		.currentBalance(account.getCurrentBalance().toBigDecimal())
        		.archived(account.isArchived() ? "Y" : "N")
        		.isDeleted(account.isDeleted() ? "Y" : "N")
        		.createdAt(account.getCreatedAt())
        		.build();
    }
}
