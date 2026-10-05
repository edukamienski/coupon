package com.eduardo.coupon.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CouponPersistenceIntegrationTest {

	private static final Instant REFERENCE = Instant.parse("2026-10-05T12:00:00Z");
	private static final Instant EXPIRATION = Instant.parse("2030-12-31T23:59:59Z");

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponJpaRepository couponJpaRepository;

	@Test
	void shouldPersistAndRetrieveCoupon() {
		Coupon coupon = newCoupon("ABC-123", "Desconto especial", "0.8", true);

		Coupon saved = couponRepository.save(coupon);
		Coupon retrieved = couponRepository.findById(saved.id()).orElseThrow();

		assertSameState(coupon, retrieved);
	}

	@Test
	void shouldPersistAllCouponFields() {
		Coupon coupon = newCoupon("ab-c12 3", "Oferta 10%", "0.50", false);

		Coupon saved = couponRepository.save(coupon);
		Coupon retrieved = couponRepository.findById(saved.id()).orElseThrow();

		assertSameState(coupon, retrieved);
		assertEquals("abc123", retrieved.code().value());
		assertFalse(retrieved.published());
	}

	@Test
	void shouldPersistDeletedStatusWithoutRemovingCoupon() {
		Coupon coupon = newCoupon("ABC-123", "Desconto especial", "0.8", true);
		UUID id = coupon.id();
		couponRepository.save(coupon);

		Coupon loaded = couponRepository.findById(id).orElseThrow();
		loaded.delete();
		couponRepository.save(loaded);

		Coupon retrieved = couponRepository.findById(id).orElseThrow();
		assertEquals(CouponStatus.DELETED, retrieved.status());
		assertEquals(id, retrieved.id());
		assertEquals(coupon.code(), retrieved.code());
		assertEquals(coupon.description(), retrieved.description());
		assertEquals(0, coupon.discountValue().compareTo(retrieved.discountValue()));
		assertEquals(coupon.expirationDate(), retrieved.expirationDate());
		assertEquals(coupon.published(), retrieved.published());
		assertEquals(coupon.redeemed(), retrieved.redeemed());
		assertTrue(couponJpaRepository.findById(id).isPresent());
	}

	@Test
	void shouldReturnEmptyWhenCouponDoesNotExist() {
		Optional<Coupon> found = couponRepository.findById(UUID.randomUUID());

		assertTrue(found.isEmpty());
	}

	@Test
	void shouldUpdateExistingCouponInsteadOfCreatingAnotherRecord() {
		Coupon coupon = newCoupon("ABC-123", "Desconto especial", "0.8", true);
		couponRepository.save(coupon);
		long countAfterInsert = couponJpaRepository.count();

		Coupon loaded = couponRepository.findById(coupon.id()).orElseThrow();
		loaded.delete();
		Coupon updated = couponRepository.save(loaded);

		assertEquals(coupon.id(), updated.id());
		assertEquals(countAfterInsert, couponJpaRepository.count());
		assertEquals(CouponStatus.DELETED, updated.status());
	}

	private static Coupon newCoupon(String rawCode, String description, String discount, boolean published) {
		Coupon coupon = Coupon.create(
				rawCode,
				description,
				new BigDecimal(discount),
				EXPIRATION,
				REFERENCE,
				published);
		coupon.attachId(UUID.randomUUID());
		return coupon;
	}

	private static void assertSameState(Coupon expected, Coupon actual) {
		assertEquals(expected.id(), actual.id());
		assertEquals(expected.code().value(), actual.code().value());
		assertEquals(expected.description(), actual.description());
		assertEquals(0, expected.discountValue().compareTo(actual.discountValue()));
		assertEquals(expected.expirationDate(), actual.expirationDate());
		assertEquals(expected.published(), actual.published());
		assertEquals(expected.redeemed(), actual.redeemed());
		assertEquals(expected.status(), actual.status());
	}
}
