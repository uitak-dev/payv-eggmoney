package com.eggmoney.payv.domain.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object: KRW(원) 전용 금액.
 * @author 정의탁
 */
public final class Money implements Comparable<Money>, Serializable {

	private static final int SCALE = 0;
	private static final RoundingMode ROUND = RoundingMode.UNNECESSARY; // 소수 있으면 예외

	private final BigDecimal amount;

	private Money(BigDecimal normalized) {
		this.amount = normalized;
	}

	// Money 생성 팩토리( 0원 )
	public static Money zero() {
		return new Money(BigDecimal.ZERO.setScale(SCALE));
	}

	/**
	 * Money 생성 팩토리.
	 * 용도: 
	 * - “원 단위 정수”가 이미 확정된 도메인 로직/테스트
	 * - 가장 안전하고 읽기 쉬움. 소수 불가가 컴파일 타임에 드러남(정수형).
	 */
	public static Money won(long v) {
		return new Money(BigDecimal.valueOf(v).setScale(SCALE));
	}

	/**
	 * Money 생성 팩토리
	 * 용도: 
	 * - DB/외부 시스템에서 넘어온 숫자(이미 정규화된 금액).
	 * - 스케일=0 강제(UNNECESSARY)로 소수 들어오면 즉시 예외. 
	 * - DB ↔ 도메인 경계에서 쓰기 좋음.
	 */
	public static Money of(BigDecimal v) {
		if (v == null)
			throw new IllegalArgumentException("amount is required");
		try {
			return new Money(v.setScale(SCALE, ROUND));
		} catch (ArithmeticException ex) {
			throw new IllegalArgumentException("KRW does not allow decimals: " + v, ex);
		}
	}
	
	/**
	 * Money 생성 팩토리
	 * 용도: 
	 * - 폼 입력/CSV 등 “문자열”로 들어오는 금액
	 * - 사용자 입력 검증 지점. “12.34” 같은 값은 예외로 차단.
	 */
	public static Money parse(String s) {
		return of(new BigDecimal(s));
	}	
	
	public boolean isZero()     { return amount.signum() == 0; }
    public boolean isPositive() { return amount.signum() > 0; }
    public boolean isNegative() { return amount.signum() < 0; }
	
    // 금액 추가.
	public Money plus(Money other) {
		return new Money(this.amount.add(other.amount).setScale(SCALE));
	}
	
	// 금액 감소.
	public Money minus(Money other) {
		return new Money(this.amount.subtract(other.amount).setScale(SCALE));
	}

	// 인프라 표시 용도
	public BigDecimal toBigDecimal() { return amount; }
	public long toLong() { return amount.longValueExact(); }

	
	@Override
	public int compareTo(Money o) {
		return this.amount.compareTo(o.amount);
	}

	@Override
	public boolean equals(Object o) {
		return (this == o) || (o instanceof Money && amount.equals(((Money) o).amount));
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount);
	}

	@Override
	public String toString() {
		return amount.toPlainString();
	}
}
