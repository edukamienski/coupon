package com.eduardo.coupon.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.eduardo.coupon.domain.exception.CouponAlreadyDeletedException;
import com.eduardo.coupon.domain.exception.InvalidDiscountException;
import com.eduardo.coupon.domain.exception.InvalidExpirationException;
import com.eduardo.coupon.domain.exception.MissingRequiredFieldException;

public final class Coupon {

	private static final BigDecimal MINIMUM_DISCOUNT = new BigDecimal("0.5");

	private UUID id;
	private final CouponCode code;
	private final String description;
	private final BigDecimal discountValue;
	private final Instant expirationDate;
	private final boolean published;
	private final boolean redeemed;
	private CouponStatus status;

	private Coupon(
			UUID id,
			CouponCode code,
			String description,
			BigDecimal discountValue,
			Instant expirationDate,
			boolean published,
			boolean redeemed,
			CouponStatus status) {
		this.id = id;
		this.code = code;
		this.description = description;
		this.discountValue = discountValue;
		this.expirationDate = expirationDate;
		this.published = published;
		this.redeemed = redeemed;
		this.status = status;
	}

	public static Coupon create(
			String rawCode,
			String rawDescription,
			BigDecimal discountValue,
			Instant expirationDate,
			Instant referenceInstant,
			boolean published) {

		UUID id = UUID.randomUUID();
		CouponCode code = CouponCode.of(rawCode);
		String description = requireDescription(rawDescription);
		BigDecimal discount = requireDiscount(discountValue);
		Instant expiration = requireExpiration(expirationDate, referenceInstant);
		return new Coupon(id, code, description, discount, expiration, published, false, CouponStatus.ACTIVE);
	}

	public static Coupon reconstitute(
			UUID id,
			String sanitizedCode,
			String description,
			BigDecimal discountValue,
			Instant expirationDate,
			boolean published,
			boolean redeemed,
			CouponStatus status) {
		return new Coupon(
				id,
				CouponCode.of(sanitizedCode),
				description,
				discountValue,
				expirationDate,
				published,
				redeemed,
				status);
	}

	public void delete() {
		if (status == CouponStatus.DELETED) {
			throw new CouponAlreadyDeletedException();
		}
		status = CouponStatus.DELETED;
	}

	public UUID id() {
		return id;
	}

	public CouponCode code() {
		return code;
	}

	public String description() {
		return description;
	}

	public BigDecimal discountValue() {
		return discountValue;
	}

	public Instant expirationDate() {
		return expirationDate;
	}

	public boolean published() {
		return published;
	}

	public boolean redeemed() {
		return redeemed;
	}

	public CouponStatus status() {
		return status;
	}

	private static String requireDescription(String rawDescription) {
		if (rawDescription == null || rawDescription.isBlank()) {
			throw new MissingRequiredFieldException("description");
		}
		return rawDescription.trim();
	}

	private static BigDecimal requireDiscount(BigDecimal discountValue) {
		if (discountValue == null) {
			throw new MissingRequiredFieldException("discountValue");
		}
		if (discountValue.compareTo(MINIMUM_DISCOUNT) < 0) {
			throw new InvalidDiscountException();
		}
		return discountValue;
	}

	private static Instant requireExpiration(Instant expirationDate, Instant referenceInstant) {
		if (expirationDate == null) {
			throw new MissingRequiredFieldException("expirationDate");
		}
		if (referenceInstant == null) {
			throw new MissingRequiredFieldException("referenceInstant");
		}
		if (expirationDate.isBefore(referenceInstant)) {
			throw new InvalidExpirationException();
		}
		return expirationDate;
	}
}
