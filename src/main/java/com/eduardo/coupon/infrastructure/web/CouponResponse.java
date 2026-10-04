package com.eduardo.coupon.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;

import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
		"id",
		"code",
		"description",
		"discountValue",
		"expirationDate",
		"status",
		"published",
		"redeemed"
})
public record CouponResponse(
		String id,
		String code,
		String description,
		BigDecimal discountValue,
		Instant expirationDate,
		CouponStatus status,
		boolean published,
		boolean redeemed) {

	public static CouponResponse from(Coupon coupon) {
		return new CouponResponse(
				coupon.id().toString(),
				coupon.code().value(),
				coupon.description(),
				coupon.discountValue(),
				coupon.expirationDate(),
				coupon.status(),
				coupon.published(),
				coupon.redeemed());
	}
}
