package com.eduardo.coupon.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;

import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

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
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, format = "uuid", example = "cef9d1e3-aae5-4ab6-a297-358c6032b1e7")
		String id,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "ABC123")
		String code,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		String description,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "0.8")
		BigDecimal discountValue,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, format = "date-time", example = "2030-12-31T23:59:59.000Z")
		Instant expirationDate,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		CouponStatus status,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		boolean published,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
