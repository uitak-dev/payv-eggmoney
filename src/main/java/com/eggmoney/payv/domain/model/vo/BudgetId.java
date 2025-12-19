package com.eggmoney.payv.domain.model.vo;

import com.eggmoney.payv.domain.shared.id.StringId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

public final class BudgetId implements StringId {

private final String value;
	
	private BudgetId(String value) {
		this.value = EntityIdentifier.nonBlank(value, "budgetId");
	}
	
	public static BudgetId of(String value) {
		return new BudgetId(value);
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
        return (o instanceof BudgetId) && ((BudgetId)o).value.equals(this.value);
    }
}
