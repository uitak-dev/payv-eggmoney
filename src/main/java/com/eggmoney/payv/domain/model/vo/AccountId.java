package com.eggmoney.payv.domain.model.vo;

import java.util.Objects;

import com.eggmoney.payv.domain.shared.id.StringId;
import com.eggmoney.payv.domain.shared.util.EntityIdentifier;

public final class AccountId implements StringId {

	private final String value;
	
	private AccountId(String value) {
		this.value = EntityIdentifier.nonBlank(value, "accountId");
	}
	
	public static AccountId of(String value) {
		return new AccountId(value);
	}
	
	public String value() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof AccountId) && ((AccountId) o).value.equals(this.value);
	}
}
