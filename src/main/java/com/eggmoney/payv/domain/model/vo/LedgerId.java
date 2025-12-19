package com.eggmoney.payv.domain.model.vo;

import java.util.Objects;

import com.eggmoney.payv.domain.shared.id.StringId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

public final class LedgerId implements StringId {

	private final String value;
    
	private LedgerId(String value) { 
		this.value = EntityIdentifier.nonBlank(value, "ledgerId");
	}
	
    public static LedgerId of(String value) { 
    	return new LedgerId(value); 
    }
    
    public String value() { 
    	return value; 
    }
    
    @Override 
    public String toString() { return value; }
    
    @Override 
    public int hashCode() { return Objects.hash(value); }
    
    @Override 
    public boolean equals(Object o) {
        return (o instanceof LedgerId) && ((LedgerId)o).value.equals(this.value);
    }
}
