package com.eggmoney.payv.infrastructure.mybatis.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.repository.CategoryRepository;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.infrastructure.mybatis.mapper.CategoryMapper;
import com.eggmoney.payv.infrastructure.mybatis.record.CategoryRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyBatisCategoryRepository implements CategoryRepository {

	private final CategoryMapper mapper;
	
	@Override
    public Optional<Category> findById(CategoryId id) {
        CategoryRecord categoryRecord = mapper.selectById(id.value());
        return Optional.ofNullable(categoryRecord).map(this::toDomain);
    }

    @Override
    public Optional<Category> findByLedgerAndName(LedgerId ledgerId, String name) {
        CategoryRecord categoryRecord = mapper.selectByLedgerAndName(ledgerId.value(), name);
        return Optional.ofNullable(categoryRecord).map(this::toDomain);
    }

    @Override
    public List<Category> findListByLedger(LedgerId ledgerId) {
        return mapper.selectListByLedger(ledgerId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Category> findRootCategoryListByLedger(LedgerId ledgerId) {
    	return mapper.selectRootCategoryListByLedger(ledgerId.value())
    			.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Category> findSubCategoryListByLedgerAndParentCategory(LedgerId ledgerId, CategoryId parentId) {
    	return mapper.selectSubCategoryListByLedgerAndParentCategory(ledgerId.value(), parentId.value())
    			.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Category category) {
        CategoryRecord rec = toRecord(category);
        // upsert: 존재하면 update, 없으면 insert
        CategoryRecord existing = mapper.selectById(category.getId().value());
        try {
            if (existing == null) mapper.insert(rec);
            else mapper.update(rec);
        } catch (DuplicateKeyException dup) {
            // UQ(LEDGER_ID, NAME) 위반 등 → 도메인 예외로 변환
            throw new DomainException("category name already exists in this ledger", dup);
        }
    }

    @Override
    public void delete(LedgerId ledgerId, CategoryId id) {
        mapper.delete(id.value());
        mapper.deleteChildren(ledgerId.value(), id.value());
    }
    
    @Override
    public List<Category> findSystemTemplatesOrdered() {
    	return mapper.selectSystemTemplatesOrdered().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
	}

    // ---- 변환부 ----
    private Category toDomain(CategoryRecord record) {
        return Category.reconstruct(
        		CategoryId.of(record.getCategoryId()),
                LedgerId.of(record.getLedgerId()),
                record.getName(),
                "Y".equals(record.getIsSystemCategory()),
                record.getParentId() == null ? null : CategoryId.of(record.getParentId()),
                record.getSortOrder() == null ? 0 : record.getSortOrder(),
                "Y".equals(record.getIsDeleted())
        );
    }

    private CategoryRecord toRecord(Category category) {
        return CategoryRecord.builder()
        		.categoryId(category.getId().value())
        		.ledgerId(category.getLedgerId().value())
        		.name(category.getName())
        		.isSystemCategory(category.isSystemCategory() ? "Y" : "N")
        		.parentId(category.getParentId() == null ? null : category.getParentId().value())
        		.sortOrder(category.getSortOrder())
        		.isDeleted(category.isDeleted() ? "Y" : "N")
        		.build();
    }
}
