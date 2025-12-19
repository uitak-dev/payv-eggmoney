package com.eggmoney.payv.domain.model.vo;

import com.eggmoney.payv.domain.shared.id.StringId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

public final class TransactionId implements StringId {
	
	private final String value;
	
	private TransactionId(String value) {
		this.value = EntityIdentifier.nonBlank(value, "transactionId");
	}
	
	public static TransactionId of(String value) {
		return new TransactionId(value);
	}
	
	public String value() {
		return value;
	}
	
	@Override 
    public String toString() { return value; }
    
    @Override 
    public int hashCode() { return value.hashCode(); }
    
    @Override 
    public boolean equals(Object o) {
        return (o instanceof TransactionId) && ((TransactionId)o).value.equals(this.value);
    }
}
