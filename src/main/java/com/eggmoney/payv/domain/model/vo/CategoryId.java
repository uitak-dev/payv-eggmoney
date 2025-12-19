package com.eggmoney.payv.domain.model.vo;

import java.util.Objects;

import com.eggmoney.payv.domain.shared.id.StringId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

public final class CategoryId implements StringId {

	private final String value;
	
	private CategoryId(String value) {
		this.value = EntityIdentifier.nonBlank(value, "categoryId");
    }

	public static CategoryId of(String value) {
		return new CategoryId(value);
	}

	public String value() {
		return value;
	}

	@Override 
    public String toString() { return value; }
	
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
        if (!(o instanceof CategoryId)) return false;
        CategoryId that = (CategoryId) o;
        return value.equals(that.value);
	}
}
