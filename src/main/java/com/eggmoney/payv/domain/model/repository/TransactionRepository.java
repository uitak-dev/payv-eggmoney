package com.eggmoney.payv.domain.model.repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.TransactionId;
import com.eggmoney.payv.presentation.dto.PageRequestDto;
import com.eggmoney.payv.presentation.dto.PageResultDto;
import com.eggmoney.payv.presentation.dto.TransactionSearchCondition;

/**
 * 거래 내역 레포지토리
 * 
 * @author 정의탁
 */
public interface TransactionRepository {

	Optional<Transaction> findById(TransactionId id);

	// UPSERT = 새로 생성된 거래는 insert, 기존이면 update
	void save(Transaction tx);

	// 게시되지 않은 거래만 삭제 가능(규칙은 서비스에서 검사)
	void delete(TransactionId id);

	// 조회 유틸: 가계부/기간 기준
	List<Transaction> findByLedgerAndDateRange(LedgerId ledgerId, LocalDate from, LocalDate to, int limit, int offset);
	
	// 조회 유틸: 필터링 검색 + 페이징
	PageResultDto<Transaction> search(LedgerId ledgerId, TransactionSearchCondition cond, PageRequestDto page);

	// 조회 유틸: 월 단위 편의 조회.
	default List<Transaction> findByLedgerAndMonth(LedgerId ledgerId, YearMonth month, int limit, int offset) {
		LocalDate from = month.atDay(1);
		LocalDate to = month.plusMonths(1).atDay(1);
		return findByLedgerAndDateRange(ledgerId, from, to, limit, offset);
	}

	// 조회 유틸: 자산별 조회.
	List<Transaction> findByLedgerAndAccount(LedgerId ledgerId, AccountId accountId, int limit, int offset);

	// 조회 유틸: 카테고리 집합(상위 선택 시, 서비스에서 자식 포함 리스트 전달)별 조회.
	List<Transaction> findByLedgerAndCategoryIds(LedgerId ledgerId, List<CategoryId> categoryIds, int limit, int offset);
}
