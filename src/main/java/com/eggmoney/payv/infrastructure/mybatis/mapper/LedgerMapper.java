package com.eggmoney.payv.infrastructure.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.infrastructure.mybatis.record.LedgerRecord;

@Mapper
public interface LedgerMapper {

	LedgerRecord selectById(@Param("ledgerId") String ledgerId);
    List<LedgerRecord> selectListByOwner(@Param("ownerId") String ownerId);
    
    // 소유자 기준 가계부 이름 중복 체크.
    Integer existsByOwnerAndName(@Param("ownerId") String ownerId, @Param("name") String name);
    
    int insert(LedgerRecord rec);
    int update(LedgerRecord rec);
}
