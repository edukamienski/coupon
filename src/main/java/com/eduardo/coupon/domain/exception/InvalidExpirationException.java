package com.eduardo.coupon.domain.exception;

public class InvalidExpirationException extends DomainException {

	public InvalidExpirationException() {
		super("A data de expiração não pode estar no passado");
	}
}
