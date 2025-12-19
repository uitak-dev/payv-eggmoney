package com.eggmoney.payv.domain.model.vo;

import com.eggmoney.payv.domain.shared.id.StringId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

public final class UserId implements StringId {

	private final String value;
    
	private UserId(String value) { 
		this.value = EntityIdentifier.nonBlank(value, "userId");
	}
	
    public static UserId of(String value) { 
    	return new UserId(value); 
    }
    
    public String value() { 
    	return value; 
    }
    
    // JSP EL에서 프로퍼티로 인식하려면 getValue() 필요
    public String getValue() {
        return value;
    }
    
    @Override 
    public String toString() { return value; }
    
    @Override 
    public int hashCode() { return value.hashCode(); }
    
    @Override 
    public boolean equals(Object o) {
        return (o instanceof UserId) && ((UserId)o).value.equals(this.value);
    }
}
