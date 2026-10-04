package com.eduardo.coupon.application.exception;

public class CouponNotFoundException extends RuntimeException {

	public CouponNotFoundException(String id) {
		super("Cupom não encontrado: " + id);
	}
}
