package com.eduardo.coupon.infrastructure.persistence;

import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;

final class CouponMapper {

	private CouponMapper() {
	}

	static CouponEntity toEntity(Coupon coupon) {
		CouponEntity entity = new CouponEntity();
		entity.setId(coupon.id());
		entity.setCode(coupon.code().value());
		entity.setDescription(coupon.description());
		entity.setDiscountValue(coupon.discountValue());
		entity.setExpirationDate(coupon.expirationDate());
		entity.setPublished(coupon.published());
		entity.setRedeemed(coupon.redeemed());
		entity.setStatus(coupon.status());
		return entity;
	}

	static Coupon toDomain(CouponEntity entity) {
		return Coupon.reconstitute(
				entity.getId(),
				entity.getCode(),
				entity.getDescription(),
				entity.getDiscountValue(),
				entity.getExpirationDate(),
				entity.isPublished(),
				entity.isRedeemed(),
				entity.getStatus() == null ? CouponStatus.ACTIVE : entity.getStatus());
	}
}
