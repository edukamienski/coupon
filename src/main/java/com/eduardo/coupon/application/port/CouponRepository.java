package com.eduardo.coupon.application.port;

import java.util.Optional;
import java.util.UUID;

import com.eduardo.coupon.domain.Coupon;

public interface CouponRepository {

	Coupon save(Coupon coupon);

	Optional<Coupon> findById(UUID id);

	Optional<Coupon> findByIDAndStatusActive(UUID id);
}
