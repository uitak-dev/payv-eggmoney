package com.eggmoney.payv.domain.model.entity;

import java.time.LocalDateTime;

import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

import lombok.Getter;

/**
 * 사용자 식별(로그인 주체), 기본 속성(email) 보관, 소유자/작성자 기준 키 제공.
 * @author 정의탁
 */
@Getter
public class User {

	private UserId id;
	private String email;
	private String password;
	private String name;
	private LocalDateTime createdAt;
	
	private User(UserId id, String email, String password, String name, LocalDateTime createdAt) {
		if (id == null) throw new IllegalArgumentException("id is required");
        if (email == null) throw new IllegalArgumentException("email is required");
        if (password == null) throw new IllegalArgumentException("password is required");
        if (name == null) throw new IllegalArgumentException("name is required");
		
		this.id = id;
		this.email = email;
		this.password = password;
		this.name = name;
		this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
	}
	
	public static User create(String email, String password, String name){
		return new User(UserId.of(EntityIdentifier.generateUuid()), 
				email, password, name, LocalDateTime.now());
	}
	
	// 인프라 복원용(레코드 → 도메인).
	public static User reconstruct(UserId id, String email, String password, 
			String name, LocalDateTime createdAt) {
		
		return new User(id, email, password, name, createdAt);
	}
	
	// 가계부 생성.
	public Ledger createLedger(String ledgerName) {
		return Ledger.create(this.id, ledgerName);
	}	
	
	// 이메일 변경.
	public void changeEmail(String newEmail){
        if (newEmail == null) {
        	throw new IllegalArgumentException("email is required");
        }
        this.email = newEmail;
    }
	
	// 이름 변경
	public void changeName(String newName) {
		this.name = newName != null ? newName.trim() : null;
	}
}
