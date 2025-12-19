package com.eggmoney.payv.infrastructure.mybatis.repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.domain.model.entity.TransactionType;
import com.eggmoney.payv.domain.model.repository.TransactionRepository;
import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.model.vo.TransactionId;
import com.eggmoney.payv.infrastructure.mybatis.mapper.TransactionMapper;
import com.eggmoney.payv.infrastructure.mybatis.record.TransactionRecord;
import com.eggmoney.payv.presentation.dto.PageRequestDto;
import com.eggmoney.payv.presentation.dto.PageResultDto;
import com.eggmoney.payv.presentation.dto.TransactionSearchCondition;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyBatisTransactionRepository  implements TransactionRepository {

	private final TransactionMapper mapper;
	
	@Override
    public Optional<Transaction> findById(TransactionId id) {
        TransactionRecord transactionRecord = mapper.selectById(id.value());
        return Optional.ofNullable(transactionRecord).map(this::toDomain);
    }

    @Override
    public void save(Transaction Transaction) {
        // upsert 스타일: 존재하면 update, 없으면 insert
        TransactionRecord existing = mapper.selectById(Transaction.getId().value());
        if (existing == null) {
            mapper.insert(toRecord(Transaction));
        } else {
            mapper.update(toRecord(Transaction));
        }
    }

    @Override
    public void delete(TransactionId id) {
        mapper.delete(id.value());
    }

    @Override
    public List<Transaction> findByLedgerAndDateRange(LedgerId ledgerId, 
    		LocalDate from, LocalDate to, int limit, int offset) {
    	
        return mapper.selectByLedgerAndDateRange(ledgerId.value(), from, to, offset, limit)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }
    
	@Override
	public List<Transaction> findByLedgerAndMonth(LedgerId ledgerId, YearMonth month, int limit, int offset) {
		LocalDate from = month.atDay(1);
		LocalDate to = month.plusMonths(1).atDay(1);
		return findByLedgerAndDateRange(ledgerId, from, to, limit, offset);
	}

	@Override
	public List<Transaction> findByLedgerAndAccount(LedgerId ledgerId, AccountId accountId, int limit, int offset) {
		return mapper.selectByLedgerAndAccount(ledgerId.value(), accountId.value(), limit, offset)
				.stream().map(this::toDomain).collect(Collectors.toList());
	}

	@Override
	public List<Transaction> findByLedgerAndCategoryIds(LedgerId ledgerId, List<CategoryId> categoryIds, int limit, int offset) {
		List<String> ids = categoryIds.stream().map(CategoryId::value).collect(Collectors.toList());
		return mapper.selectByLedgerAndCategoryIds(ledgerId.value(), ids, limit, offset)
				.stream().map(this::toDomain).collect(Collectors.toList());
	}
    
	
	@Override
	public PageResultDto<Transaction> search(LedgerId ledgerId, 
											 TransactionSearchCondition cond, 
											 PageRequestDto page) {		

		// ---------- 3) 총건수/목록 조회 ----------
		long total = mapper.countByCond(ledgerId.toString(), cond);

		// (요청 페이지가 범위를 벗어나면 마지막 페이지로 조정하는 로직이 필요하면 여기서 보정 가능)
		List<Transaction> content = mapper.listByCond(ledgerId.toString(), cond, page.offset(), page.limit())
				.stream().map(this::toDomain).collect(Collectors.toList());

		// ---------- 4) 페이지 결과 조립 ----------
		return new PageResultDto<>(total, page.getPage(), page.getSize(), content);
	}
	
	
	

    // ---- 변환부 ----
    private Transaction toDomain(TransactionRecord record) {
        return Transaction.reconstruct(
            TransactionId.of(record.getTransactionId()),
            LedgerId.of(record.getLedgerId()),
            AccountId.of(record.getAccountId()),
            TransactionType.valueOf(record.getType()),
            record.getDate(),
            Money.of(record.getAmount()),                 // KRW scale=0
            CategoryId.of(record.getCategoryId()),
            record.getMemo(),
            "Y".equals(record.getPosted()),
            record.getPostedAt(),
            record.getCreatedAt()
        );
    }

	private TransactionRecord toRecord(Transaction transaction) {
		return TransactionRecord.builder()
				.transactionId(transaction.getId().value())
				.ledgerId(transaction.getLedgerId().value())
				.accountId(transaction.getAccountId().value())
				.date(transaction.getDate())
				.type(transaction.getType().name())
				.amount(transaction.getAmount().toBigDecimal())
				.categoryId(transaction.getCategoryId().value())
				.memo(transaction.getMemo())
				.posted(transaction.isPosted() ? "Y" : "N")
				.postedAt(transaction.getPostedAt())
				.createdAt(transaction.getCreatedAt())
				.build();
	}
}
