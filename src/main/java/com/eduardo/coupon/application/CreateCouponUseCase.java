package com.eduardo.coupon.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;

public final class CreateCouponUseCase {

	private final CouponRepository couponRepository;

	public CreateCouponUseCase(CouponRepository couponRepository) {
		this.couponRepository = couponRepository;
	}

	public Coupon execute(
			String code,
			String description,
			BigDecimal discountValue,
			Instant expirationDate,
			boolean published) {
		Coupon coupon = Coupon.create(code, description, discountValue, expirationDate, Instant.now(), published);
		coupon.attachId(UUID.randomUUID());
		return couponRepository.save(coupon);
	}
}
