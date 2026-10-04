package com.eduardo.coupon.domain;

import java.util.Objects;

import com.eduardo.coupon.domain.exception.InvalidCouponCodeException;
import com.eduardo.coupon.domain.exception.MissingRequiredFieldException;

public final class CouponCode {

	private final String value;

	private CouponCode(String value) {
		this.value = value;
	}

	public static CouponCode of(String raw) {
		if (raw == null) {
			throw new MissingRequiredFieldException("code");
		}
		String sanitized = raw.replaceAll("[^A-Za-z0-9]", "");
		if (sanitized.length() != 6) {
			throw new InvalidCouponCodeException();
		}
		return new CouponCode(sanitized);
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CouponCode couponCode)) {
			return false;
		}
		return value.equals(couponCode.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
}
