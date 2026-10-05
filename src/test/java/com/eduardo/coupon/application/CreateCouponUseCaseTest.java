package com.eduardo.coupon.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;

class CreateCouponUseCaseTest {

	private static final String RAW_CODE = "ABC-123";
	private static final String SANITIZED_CODE = "ABC123";
	private static final String DESCRIPTION = "Desconto de lancamento";
	private static final BigDecimal DISCOUNT = new BigDecimal("0.8");
	private static final Instant EXPIRATION = Instant.parse("2030-12-31T23:59:59Z");
	private static final boolean PUBLISHED = true;

	@Test
	void returnsTheCouponSavedByTheRepository() {
		Coupon persisted = persistedCoupon();
		RecordingCouponRepository couponRepository = new RecordingCouponRepository(persisted);
		CreateCouponUseCase useCase = new CreateCouponUseCase(couponRepository);

		Coupon result = useCase.execute(RAW_CODE, DESCRIPTION, DISCOUNT, EXPIRATION, PUBLISHED);

		assertSame(persisted, result);
	}

	@Test
	void savesTheCreatedCouponOnce() {
		RecordingCouponRepository couponRepository = new RecordingCouponRepository(persistedCoupon());
		CreateCouponUseCase useCase = new CreateCouponUseCase(couponRepository);

		useCase.execute(RAW_CODE, DESCRIPTION, DISCOUNT, EXPIRATION, PUBLISHED);

		assertEquals(1, couponRepository.saved.size());
		Coupon saved = couponRepository.saved.get(0);
		assertEquals(SANITIZED_CODE, saved.code().value());
		assertEquals(DESCRIPTION, saved.description());
		assertEquals(DISCOUNT, saved.discountValue());
		assertEquals(EXPIRATION, saved.expirationDate());
		assertEquals(PUBLISHED, saved.published());
		assertEquals(CouponStatus.ACTIVE, saved.status());
		assertFalse(saved.redeemed());
		assertNotNull(saved.id());
	}

	private static Coupon persistedCoupon() {
		return Coupon.reconstitute(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"XYZ789",
				"retorno do repositorio",
				DISCOUNT,
				EXPIRATION,
				false,
				false,
				CouponStatus.ACTIVE);
	}

	private static final class RecordingCouponRepository implements CouponRepository {

		private final List<Coupon> saved = new ArrayList<>();
		private final Coupon toReturn;

		private RecordingCouponRepository(Coupon toReturn) {
			this.toReturn = toReturn;
		}

		@Override
		public Coupon save(Coupon coupon) {
			saved.add(coupon);
			return toReturn;
		}

		@Override
		public Optional<Coupon> findById(UUID id) {
			return Optional.empty();
		}

		@Override
		public Optional<Coupon> findByIDAndStatusActive(UUID id) {
			return Optional.empty();
		}
	}
}
