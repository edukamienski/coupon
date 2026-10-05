package com.eduardo.coupon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.eduardo.coupon.domain.exception.CouponAlreadyDeletedException;
import com.eduardo.coupon.domain.exception.InvalidCouponCodeException;
import com.eduardo.coupon.domain.exception.InvalidDiscountException;
import com.eduardo.coupon.domain.exception.InvalidExpirationException;
import com.eduardo.coupon.domain.exception.MissingRequiredFieldException;

class CouponTest {

	private static final Instant REFERENCE = Instant.parse("2026-10-04T15:00:00Z");
	private static final Instant FUTURE = REFERENCE.plusSeconds(3600);
	private static final BigDecimal VALID_DISCOUNT = new BigDecimal("0.8");
	private static final String VALID_CODE = "ABC123";
	private static final String VALID_DESCRIPTION = "descricao";

	@Test
	void nullCodeIsRejectedByCouponCode() {
		MissingRequiredFieldException exception = assertThrows(
				MissingRequiredFieldException.class,
				() -> CouponCode.of(null));

		assertEquals("Campo obrigatório ausente: code", exception.getMessage());
	}

	@Test
	void sanitizesCodeRemovingNonAlphanumericAndPreservingCase() {
		Coupon hyphenated = create("ABC-123", VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE, false);
		assertEquals("ABC123", hyphenated.code().value());

		Coupon mixedCase = create("aB-c12 3", VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE, false);
		assertEquals("aBc123", mixedCase.code().value());
	}

	@Test
	void rejectsCodeWithFiveCharactersAfterSanitization() {
		assertThrows(InvalidCouponCodeException.class, () -> create(
				"AB-C12",
				VALID_DESCRIPTION,
				VALID_DISCOUNT,
				FUTURE,
				false));
	}

	@Test
	void rejectsCodeWithSevenCharactersAfterSanitization() {
		assertThrows(InvalidCouponCodeException.class, () -> create(
				"ABC-1234",
				VALID_DESCRIPTION,
				VALID_DISCOUNT,
				FUTURE,
				false));
	}

	@Test
	void rejectsCodeMadeOnlyOfSpecialCharacters() {
		assertThrows(InvalidCouponCodeException.class, () -> create(
				"---",
				VALID_DESCRIPTION,
				VALID_DISCOUNT,
				FUTURE,
				false));
	}

	@Test
	void nullCodeDoesNotCreateCoupon() {
		assertMissingField("code", () -> create(null, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE, false));
	}

	@Test
	void nullDescriptionDoesNotCreateCoupon() {
		assertMissingField("description", () -> create(VALID_CODE, null, VALID_DISCOUNT, FUTURE, false));
	}

	@Test
	void nullDiscountDoesNotCreateCoupon() {
		assertMissingField("discountValue", () -> create(VALID_CODE, VALID_DESCRIPTION, null, FUTURE, false));
	}

	@Test
	void nullExpirationDoesNotCreateCoupon() {
		assertMissingField("expirationDate", () -> create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, null, false));
	}

	@Test
	void emptyDescriptionDoesNotCreateCoupon() {
		assertMissingField("description", () -> create(VALID_CODE, "", VALID_DISCOUNT, FUTURE, false));
	}

	@Test
	void whitespaceDescriptionDoesNotCreateCoupon() {
		assertMissingField("description", () -> create(VALID_CODE, "   ", VALID_DISCOUNT, FUTURE, false));
	}

	@Test
	void trimsDescriptionWithoutRemovingSpecialCharacters() {
		Coupon coupon = create(VALID_CODE, "  Oferta 10%  ", VALID_DISCOUNT, FUTURE, false);

		assertEquals("Oferta 10%", coupon.description());
	}

	@Test
	void rejectsDiscountBelowMinimum() {
		assertThrows(InvalidDiscountException.class, () -> create(
				VALID_CODE,
				VALID_DESCRIPTION,
				new BigDecimal("0.49"),
				FUTURE,
				false));
	}

	@Test
	void rejectsNegativeDiscount() {
		assertThrows(InvalidDiscountException.class, () -> create(
				VALID_CODE,
				VALID_DESCRIPTION,
				new BigDecimal("-0.1"),
				FUTURE,
				false));
	}

	@Test
	void acceptsMinimumDiscount() {
		BigDecimal discount = new BigDecimal("0.50");

		Coupon coupon = create(VALID_CODE, VALID_DESCRIPTION, discount, FUTURE, false);

		assertEquals(0, discount.compareTo(coupon.discountValue()));
	}

	@Test
	void acceptsDiscountOfZeroPointEight() {
		BigDecimal discount = new BigDecimal("0.8");

		Coupon coupon = create(VALID_CODE, VALID_DESCRIPTION, discount, FUTURE, false);

		assertEquals(0, discount.compareTo(coupon.discountValue()));
	}

	@Test
	void acceptsDiscountFarAboveMinimum() {
		BigDecimal discount = new BigDecimal("1000");

		Coupon coupon = create(VALID_CODE, VALID_DESCRIPTION, discount, FUTURE, false);

		assertEquals(0, discount.compareTo(coupon.discountValue()));
	}

	@Test
	void rejectsExpirationBeforeReference() {
		assertThrows(InvalidExpirationException.class, () -> create(
				VALID_CODE,
				VALID_DESCRIPTION,
				VALID_DISCOUNT,
				REFERENCE.minusSeconds(1),
				false));
	}

	@Test
	void acceptsExpirationEqualToReference() {
		Coupon coupon = create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, REFERENCE, false);

		assertEquals(REFERENCE, coupon.expirationDate());
	}

	@Test
	void publishedTrueCreatesActiveCoupon() {
		Coupon coupon = create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE, true);

		assertTrue(coupon.published());
		assertEquals(CouponStatus.ACTIVE, coupon.status());
		assertFalse(coupon.redeemed());
		assertNull(coupon.id());
	}

	@Test
	void publishedFalseCreatesActiveCoupon() {
		Coupon coupon = create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE, false);

		assertFalse(coupon.published());
		assertEquals(CouponStatus.ACTIVE, coupon.status());
		assertFalse(coupon.redeemed());
		assertNull(coupon.id());
	}

	@Test
	void firstDeleteChangesOnlyStatusToDeleted() {
		Instant expiration = FUTURE;
		BigDecimal discount = new BigDecimal("0.8");
		Coupon coupon = create("ABC-123", "  Oferta 10%  ", discount, expiration, true);
		CouponCode code = coupon.code();
		String description = coupon.description();

		coupon.delete();

		assertEquals(CouponStatus.DELETED, coupon.status());
		assertEquals(code, coupon.code());
		assertEquals("ABC123", coupon.code().value());
		assertEquals(description, coupon.description());
		assertEquals("Oferta 10%", coupon.description());
		assertEquals(0, discount.compareTo(coupon.discountValue()));
		assertEquals(expiration, coupon.expirationDate());
		assertTrue(coupon.published());
		assertFalse(coupon.redeemed());
		assertNull(coupon.id());
	}

	@Test
	void secondDeleteIsRejected() {
		Coupon coupon = create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE, false);
		coupon.delete();

		assertThrows(CouponAlreadyDeletedException.class, coupon::delete);
		assertEquals(CouponStatus.DELETED, coupon.status());
		assertEquals(VALID_CODE, coupon.code().value());
		assertEquals(VALID_DESCRIPTION, coupon.description());
		assertEquals(0, VALID_DISCOUNT.compareTo(coupon.discountValue()));
		assertEquals(FUTURE, coupon.expirationDate());
		assertFalse(coupon.published());
		assertFalse(coupon.redeemed());
		assertNull(coupon.id());
	}

	@Test
	void deletesReconstitutedCouponWhoseExpirationIsInThePast() {
		Instant past = REFERENCE.minusSeconds(60);
		UUID id = UUID.fromString("598c5a85-46d6-4c69-8513-6ecfb9b5d7e2");
		Coupon coupon = Coupon.reconstitute(
				id,
				VALID_CODE,
				VALID_DESCRIPTION,
				VALID_DISCOUNT,
				past,
				false,
				false,
				CouponStatus.ACTIVE);

		assertEquals(CouponStatus.ACTIVE, coupon.status());
		assertEquals(past, coupon.expirationDate());

		coupon.delete();

		assertEquals(CouponStatus.DELETED, coupon.status());
		assertEquals(past, coupon.expirationDate());
		assertEquals(id, coupon.id());
		assertEquals(VALID_CODE, coupon.code().value());
		assertEquals(VALID_DESCRIPTION, coupon.description());
		assertEquals(0, VALID_DISCOUNT.compareTo(coupon.discountValue()));
		assertFalse(coupon.published());
		assertFalse(coupon.redeemed());
	}

	private static Coupon create(
			String code,
			String description,
			BigDecimal discount,
			Instant expiration,
			boolean published) {
		return Coupon.create(code, description, discount, expiration, REFERENCE, published);
	}

	private static void assertMissingField(String field, Executable executable) {
		MissingRequiredFieldException exception = assertThrows(MissingRequiredFieldException.class, executable);
		assertEquals("Campo obrigatório ausente: " + field, exception.getMessage());
	}
}
