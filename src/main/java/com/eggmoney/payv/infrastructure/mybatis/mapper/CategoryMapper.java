package com.eggmoney.payv.infrastructure.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

import com.eggmoney.payv.infrastructure.mybatis.record.CategoryRecord;

@Mapper
public interface CategoryMapper {

	CategoryRecord selectById(@Param("id") String id);
	
	// 한 가계부에 동일한 이름을 갖는 카테고리가 존재하는지 확인.
    CategoryRecord selectByLedgerAndName(@Param("ledgerId") String ledgerId, @Param("name") String name);
    
    List<CategoryRecord> selectListByLedger(@Param("ledgerId") String ledgerId);
    List<CategoryRecord> selectRootCategoryListByLedger(@Param("ledgerId") String ledgerId);
    List<CategoryRecord> selectSubCategoryListByLedgerAndParentCategory(@Param("ledgerId") String ledgerId,
    													   				@Param("parentId") String parentId);
    
    int insert(CategoryRecord rec);
    int update(CategoryRecord rec);
    
    // 소프트 삭제.
    int delete(@Param("id") String id);
    
    // 자식 일괄 소프트 삭제.
    int deleteChildren(@Param("ledgerId") String ledgerId, @Param("parentId") String parentId);
    
    @ResultMap("CategoryMap")
    List<CategoryRecord> selectSystemTemplatesOrdered();
}
