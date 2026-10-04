package com.eduardo.coupon.application;

import java.util.UUID;

import com.eduardo.coupon.application.exception.CouponNotFoundException;
import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;

public final class DeleteCouponUseCase {

	private final CouponRepository couponRepository;

	public DeleteCouponUseCase(CouponRepository couponRepository) {
		this.couponRepository = couponRepository;
	}

	public void execute(UUID id) {
		Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new CouponNotFoundException(id.toString()));
		coupon.delete();
		couponRepository.save(coupon);
	}
}
