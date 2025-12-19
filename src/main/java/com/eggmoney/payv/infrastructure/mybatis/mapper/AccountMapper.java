package com.eggmoney.payv.infrastructure.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eggmoney.payv.infrastructure.mybatis.record.AccountRecord;
import com.eggmoney.payv.infrastructure.mybatis.record.CategoryRecord;

@Mapper
public interface AccountMapper {

	AccountRecord selectById(@Param("id") String id);
	
	// 한 가계부에 동일한 이름을 갖는 자산이 존재하는지 확인.
	AccountRecord selectByLedgerAndName(@Param("ledgerId") String ledgerId, @Param("name") String name);
	
	List<AccountRecord> selectListByLedger(@Param("ledgerId") String ledgerId);
	
    int insert(AccountRecord rec);   // 새로 생성된 자산
    int update(AccountRecord rec);   // 기존 자산
    
    // 소프트 삭제.
    int delete(@Param("id") String id);
}
