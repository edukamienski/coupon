package com.eduardo.coupon.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCouponRequest(
		@NotBlank
		@Schema(example = "ABC-123")
		String code,
		@NotBlank String description,
		@NotNull
		@Schema(example = "0.8")
		BigDecimal discountValue,
		@NotNull
		@Schema(format = "date-time", example = "2030-12-31T23:59:59.000Z")
		Instant expirationDate,
		@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "false", example = "false")
		Boolean published) {
}
