package com.eduardo.coupon.domain.exception;

public class CouponAlreadyDeletedException extends DomainException {

	public CouponAlreadyDeletedException() {
		super("O cupom já foi excluído");
	}
}
