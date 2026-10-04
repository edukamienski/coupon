package com.eduardo.coupon.infrastructure.web;

import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduardo.coupon.application.CreateCouponUseCase;
import com.eduardo.coupon.application.DeleteCouponUseCase;
import com.eduardo.coupon.application.GetCouponUseCase;
import com.eduardo.coupon.domain.Coupon;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/coupon")
@AllArgsConstructor
public class CouponController {

	private final CreateCouponUseCase createCouponUseCase;
	private final GetCouponUseCase getCouponUseCase;
	private final DeleteCouponUseCase deleteCouponUseCase;

	@PostMapping
	public ResponseEntity<CouponResponse> create(@Valid @RequestBody CreateCouponRequest request) {
		boolean published = Boolean.TRUE.equals(request.published());
		Coupon coupon = createCouponUseCase.execute(
				request.code(),
				request.description(),
				request.discountValue(),
				request.expirationDate(),
				published);
		return ResponseEntity.status(201).body(CouponResponse.from(coupon));
	}

	@GetMapping("/{id}")
	public CouponResponse get(@PathVariable UUID id) {
		return CouponResponse.from(getCouponUseCase.execute(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		deleteCouponUseCase.execute(id);
		return ResponseEntity.noContent().build();
	}
}
