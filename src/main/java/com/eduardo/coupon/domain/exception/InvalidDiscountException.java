package com.eduardo.coupon.domain.exception;

public class InvalidDiscountException extends DomainException {

	public InvalidDiscountException() {
		super("O desconto deve ser maior ou igual a 0.5");
	}
}
