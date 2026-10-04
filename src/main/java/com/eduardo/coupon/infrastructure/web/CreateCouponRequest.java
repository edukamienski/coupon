package com.eduardo.coupon.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCouponRequest(
		@NotBlank String code,
		@NotBlank String description,
		@NotNull BigDecimal discountValue,
		@NotNull Instant expirationDate,
		Boolean published) {
}
