package com.eggmoney.payv.domain.model.entity;

import java.util.Objects;

import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

import lombok.Getter;

/**
 * 수입/지출 카테고리 정의 및 계층(부모-자식) 구조.
 * 
 * 카테고리(최대 2 depth)
 *  - depth=1: 루트 (parentId == null)
 *  - depth=2: 자식 (parentId != null, 단 부모는 반드시 루트여야 함)
 *  - 손자(3단계) 이상 금지 → 도메인 서비스에서 검증
 *  
 * @author 정의탁
 */
@Getter
public class Category {
	
	private CategoryId id;
	private LedgerId ledgerId;
    private String name;
    private boolean isSystemCategory;
    private CategoryId parentId;
    private int sortOrder;
    private boolean isDeleted;

    private Category(CategoryId id, LedgerId ledgerId, CategoryId parentId, String name, 
    		boolean isSystemCategory, int sortOrder, boolean isDeleted) {
    	if (id == null) throw new IllegalArgumentException("id is required");
        if (ledgerId == null) throw new IllegalArgumentException("ledgerId is required");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name is required");
        if (sortOrder < 0) throw new IllegalArgumentException("정렬 순서는 '0'보다 커야 합니다.(오름차순)");
    	
		this.id = id;
		this.ledgerId = ledgerId;
		this.name = name;
		this.isSystemCategory = isSystemCategory;
		this.parentId = parentId;
		this.sortOrder = sortOrder;
		this.isDeleted = isDeleted;
	}
    
	// 루트 카테고리 생성 (depth=1)
    public static Category createRoot(LedgerId ledgerId, String name, 
    		boolean isSystem, int sortOrder) {
    	
        if (name == null || name.trim().isEmpty()) {
        	throw new IllegalArgumentException("name is required");
        }
        return new Category(CategoryId.of(EntityIdentifier.generateUuid()), ledgerId, null, name, 
        		isSystem, sortOrder, false);
    }
    
    // 자식 카테고리 생성 (depth=2) — 실제 검증은 서비스에서 수행
    public static Category createChild(LedgerId ledgerId, CategoryId parentId, String name, 
    		boolean isSystem, int sortOrder) {
        
    	return new Category(CategoryId.of(EntityIdentifier.generateUuid()), ledgerId, parentId, name, 
    			isSystem, sortOrder, false);
    }
	
    // 인프라 복원용 (레코드 → 도메인)
    public static Category reconstruct(CategoryId id, LedgerId ledgerId, String name, 
    		boolean isSystem, CategoryId parentId, int sortOrder, boolean isDeleted) {
    	return new Category(id, ledgerId, parentId, name, isSystem, sortOrder, isDeleted);
    }
    
    /** 
     * ---- 도메인 책임 (SSOT) ---- 
     */
    // 카테고리 이름 변경.
    public void rename(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
        	throw new IllegalArgumentException("name is required");
        }
        // 시스템 등록 카테고리는 수정/삭제 불가.
        if (this.isSystemCategory) {
        	throw new DomainException("system category cannot be renamed");
        }
        this.name = newName.trim();
    }
    
    // 카테고리 정렬 순서 변경.
    public void reorder(int newSortOrder) {
        if (newSortOrder < 0) {
        	throw new IllegalArgumentException("sortOrder must be >= 0");
        }
        // 시스템 등록 카테고리는 수정/삭제 불가.
        if (this.isSystemCategory) {
        	throw new DomainException("system category cannot be sortOrder");
        }
        this.sortOrder = newSortOrder;
    }
    
    public boolean isRoot() { return parentId == null; }
    public void delete() { this.isDeleted = true; }
    
    @Override 
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category other = (Category) o;
        return id != null && id.equals(other.getId());
    }
    
    @Override 
    public int hashCode() { 
    	return Objects.hash(id); 
    }

	@Override
	public String toString() {
		return "Category [id=" + id + ", ledgerId=" + ledgerId + ", name=" + name + ", isSystemCategory="
				+ isSystemCategory + ", parentId=" + parentId + ", sortOrder=" + sortOrder + ", isDeleted=" + isDeleted
				+ "]";
	}
    
    
}
