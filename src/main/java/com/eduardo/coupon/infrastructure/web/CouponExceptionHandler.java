package com.eduardo.coupon.infrastructure.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.swagger.v3.oas.annotations.Hidden;

import com.eduardo.coupon.application.exception.CouponNotFoundException;
import com.eduardo.coupon.domain.exception.CouponAlreadyDeletedException;
import com.eduardo.coupon.domain.exception.DomainException;

@Hidden
@RestControllerAdvice
public class CouponExceptionHandler {

	@ExceptionHandler(CouponNotFoundException.class)
	public ResponseEntity<ErrorResponse> notFound(CouponNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> invalidPath(MethodArgumentTypeMismatchException exception) {
		if (UUID.class.equals(exception.getRequiredType())) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("Cupom não encontrado: " + exception.getValue()));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Requisição inválida"));
	}

	@ExceptionHandler(CouponAlreadyDeletedException.class)
	public ResponseEntity<ErrorResponse> alreadyDeleted(CouponAlreadyDeletedException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler({DomainException.class, MethodArgumentNotValidException.class})
	public ResponseEntity<ErrorResponse> badRequest(Exception exception) {
		String message = exception instanceof MethodArgumentNotValidException
				? "Requisição inválida"
				: exception.getMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Requisição inválida"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> unexpected(Exception exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Erro interno"));
	}
}
