package com.eggmoney.payv.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

import lombok.Builder;
import lombok.Getter;

/**
 * 가계부 일관성 경계(소유자/이름/생성일), 하위 엔티티 생성/소속 조율.
 * @author 정의탁
 */
@Getter
public class Ledger {

	private LedgerId id;
	private UserId ownerId;
	private String name;
	private LocalDateTime createdAt;

	private Ledger(LedgerId id, UserId ownerId, String name, LocalDateTime createdAt) {
		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.createdAt = createdAt;
	}
	
	private Ledger(LedgerId id, UserId ownerId, String name) {
		if (id == null) throw new IllegalArgumentException("id is required");
        if (ownerId == null) throw new IllegalArgumentException("ownerId is required");
        if (name == null) throw new IllegalArgumentException("name is required");
        
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.createdAt = LocalDateTime.now();
	}
	
	public static Ledger create(UserId ownerId, String name) {
		return new Ledger(LedgerId.of(EntityIdentifier.generateUuid()), ownerId, name);
	}
	
	// 인프라 복원용 (레코드 → 도메인)
	public static Ledger reconstruct(LedgerId id, UserId ownerId, String name, LocalDateTime createdAt) {
		return new Ledger(id, ownerId, name, createdAt);
	}
	
	/**
     * ----- 도메인 책임 (SSOT) -----
     */	
	// 가계부 이름(제목) 변경.
	public void rename(String newName){
        if (newName == null || newName.trim().isEmpty()) {
        	throw new IllegalArgumentException("name is required");
        }        
        this.name = newName.trim();
    }
	
	@Override 
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ledger)) return false;
        Ledger other = (Ledger) o;
        return id != null && id.equals(other.getId());
    }

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
