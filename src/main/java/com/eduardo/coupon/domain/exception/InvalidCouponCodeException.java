package com.eduardo.coupon.domain.exception;

public class InvalidCouponCodeException extends DomainException {

	public InvalidCouponCodeException() {
		super("O código deve ter exatamente 6 caracteres alfanuméricos após a sanitização");
	}
}
