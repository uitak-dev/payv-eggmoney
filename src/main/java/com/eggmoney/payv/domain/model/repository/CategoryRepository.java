package com.eggmoney.payv.domain.model.repository;

import java.util.List;
import java.util.Optional;

import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;

/**
 * 카테고리 레포지토리
 * @author 정의탁
 */
public interface CategoryRepository {

	Optional<Category> findById(CategoryId id);
	
	// 한 가계부에 동일한 이름을 갖는 카테고리가 존재하는지 확인.
	Optional<Category> findByLedgerAndName(LedgerId ledgerId, String name);

	// 가계부에 포함된 모든 카테고리 목록 조회.
	List<Category> findListByLedger(LedgerId ledgerId);

    // 특정 가계부의 루트 카테고리 목록 조회.
    List<Category> findRootCategoryListByLedger(LedgerId ledgerId);    
	// 특정 가계부내 특정 카테고리의 하위 카테고리 목록 조회.
    List<Category> findSubCategoryListByLedgerAndParentCategory(LedgerId ledgerId, CategoryId parentId);
	
	// UPSERT = 새 UUID면 insert, 아니면 update
    void save(Category category);
    
    // 소프트 삭제.(상위 카테고리 삭제 시, 하위 카테고리 CASCADE)
    void delete(LedgerId ledgerId, CategoryId id);
    
    // 시스템 카테고리 목록 조회.( 루트→자식 순서 반환 )
    List<Category> findSystemTemplatesOrdered();
}