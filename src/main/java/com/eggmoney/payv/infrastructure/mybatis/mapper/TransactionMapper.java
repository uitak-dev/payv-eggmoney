package com.eggmoney.payv.infrastructure.mybatis.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.infrastructure.mybatis.record.TransactionRecord;
import com.eggmoney.payv.presentation.dto.TransactionSearchCondition;

@Mapper
public interface TransactionMapper {

	TransactionRecord selectById(@Param("transactionId") String transactionId);

    int insert(TransactionRecord rec);
    int update(TransactionRecord rec);
    int delete(@Param("transactionId") String transactionId);

    List<TransactionRecord> selectByLedgerAndDateRange(
            @Param("ledgerId") String ledgerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
    
    long countByCond(@Param("ledgerId") String ledgerId, @Param("cond") TransactionSearchCondition cond);
    
    List<TransactionRecord> listByCond(@Param("ledgerId") String ledgerId,
                        @Param("cond") TransactionSearchCondition cond,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

    // 자산별 거래 내역 조회.
    List<TransactionRecord> selectByLedgerAndAccount(@Param("ledgerId") String ledgerId,
                                                     @Param("accountId") String accountId,
                                                     @Param("limit") int limit,
                                                     @Param("offset") int offset);

    // 카테고리별(하위 카테고리 포함) 거래 내역 조회.
    List<TransactionRecord> selectByLedgerAndCategoryIds(@Param("ledgerId") String ledgerId,
                                                         @Param("categoryIds") List<String> categoryIds,
                                                         @Param("limit") int limit,
                                                         @Param("offset") int offset);
}
