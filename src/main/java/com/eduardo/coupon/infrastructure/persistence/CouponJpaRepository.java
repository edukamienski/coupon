package com.eduardo.coupon.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduardo.coupon.domain.CouponStatus;

interface CouponJpaRepository extends JpaRepository<CouponEntity, UUID> {

	Optional<CouponEntity> findByIdAndStatus(UUID id, CouponStatus status);
}
