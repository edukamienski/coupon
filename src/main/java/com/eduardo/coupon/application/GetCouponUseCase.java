package com.eduardo.coupon.application;

import java.util.UUID;

import com.eduardo.coupon.application.exception.CouponNotFoundException;
import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;

public final class GetCouponUseCase {

	private final CouponRepository couponRepository;

	public GetCouponUseCase(CouponRepository couponRepository) {
		this.couponRepository = couponRepository;
	}

	public Coupon execute(UUID id) {
		Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new CouponNotFoundException(id.toString()));
		if (coupon.status() == CouponStatus.DELETED) {
			throw new CouponNotFoundException(id.toString());
		}
		return coupon;
	}
}
