package com.eduardo.coupon.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;

@Repository
public class CouponRepositoryAdapter implements CouponRepository {

	private final CouponJpaRepository couponJpaRepository;

	public CouponRepositoryAdapter(CouponJpaRepository couponJpaRepository) {
		this.couponJpaRepository = couponJpaRepository;
	}

	@Override
	@Transactional
	public Coupon save(Coupon coupon) {
		CouponEntity saved = couponJpaRepository.save(CouponMapper.toEntity(coupon));
		return CouponMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Coupon> findById(UUID id) {
		return couponJpaRepository.findById(id).map(CouponMapper::toDomain);
	}
}
