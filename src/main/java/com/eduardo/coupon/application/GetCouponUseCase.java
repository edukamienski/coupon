package com.eduardo.coupon.application;

import java.util.UUID;

import com.eduardo.coupon.application.exception.CouponNotFoundException;
import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;

public final class GetCouponUseCase {

	private final CouponRepository couponRepository;

	public GetCouponUseCase(CouponRepository couponRepository) {
		this.couponRepository = couponRepository;
	}

	public Coupon execute(UUID id) {
		return couponRepository.findById(id).orElseThrow(() -> new CouponNotFoundException(id.toString()));
	}
}
