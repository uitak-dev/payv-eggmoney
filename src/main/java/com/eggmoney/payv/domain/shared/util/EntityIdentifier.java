package com.eggmoney.payv.domain.shared.util;

import java.util.UUID;

/** 엔티티 식별자 ID 생성 및 검증 유틸: 기본 UUID */
public class EntityIdentifier {

	public static String generateUuid() {
    	return UUID.randomUUID().toString();
    }
	
	public static String nonBlank(String v, String name) {
        if (v == null || v.trim().isEmpty()) 
        	throw new IllegalArgumentException(name + " is blank");
        
        return v;
    }
    
    public static long positive(long v, String name) {
        if (v <= 0) 
        	throw new IllegalArgumentException(name + " must be positive");
        
        return v;
    }
}
