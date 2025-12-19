package com.eggmoney.payv.infrastructure.mybatis.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.repository.LedgerRepository;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.infrastructure.mybatis.mapper.LedgerMapper;
import com.eggmoney.payv.infrastructure.mybatis.record.LedgerRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyBatisLedgerRepository implements LedgerRepository {

	private final LedgerMapper mapper;
	
	@Override
    public Optional<Ledger> findById(LedgerId id) {
        LedgerRecord r = mapper.selectById(id.value());
        return Optional.ofNullable(r).map(this::toDomain);
    }

    @Override
    public List<Ledger> findListByOwner(UserId ownerId) {
        return mapper.selectListByOwner(ownerId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByOwnerAndName(UserId ownerId, String name) {
        Integer one = mapper.existsByOwnerAndName(ownerId.value(), name);
        return one != null && one == 1;
    }

    @Override
    public void save(Ledger ledger) {
        LedgerRecord existing = mapper.selectById(ledger.getId().value());
        if (existing == null) {
            mapper.insert(toRecord(ledger));
        } else {
            mapper.update(toRecord(ledger));
        }
    }

    private Ledger toDomain(LedgerRecord ledgerRecord) {        
        return Ledger.reconstruct(LedgerId.of(ledgerRecord.getLedgerId()), 
        		UserId.of(ledgerRecord.getOwnerId()), ledgerRecord.getName(), ledgerRecord.getCreatedAt());
    }

    private LedgerRecord toRecord(Ledger ledger) {
        return LedgerRecord.builder()
        		.ledgerId(ledger.getId().value())
        		.ownerId(ledger.getOwnerId().value())
        		.name(ledger.getName())
        		.createdAt(ledger.getCreatedAt())
        		.build();
    }
}
