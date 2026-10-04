package com.eduardo.coupon.domain.exception;

public class MissingRequiredFieldException extends DomainException {

	public MissingRequiredFieldException(String field) {
		super("Campo obrigatório ausente: " + field);
	}
}
