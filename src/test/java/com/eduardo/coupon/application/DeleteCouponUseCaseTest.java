package com.eduardo.coupon.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.eduardo.coupon.application.exception.CouponNotFoundException;
import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;
import com.eduardo.coupon.domain.exception.CouponAlreadyDeletedException;

class DeleteCouponUseCaseTest {

	private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final String CODE = "XYZ789";
	private static final String DESCRIPTION = "retorno do repositorio";
	private static final BigDecimal DISCOUNT = new BigDecimal("0.8");
	private static final Instant EXPIRATION = Instant.parse("2030-12-31T23:59:59Z");
	private static final boolean PUBLISHED = false;
	private static final boolean REDEEMED = false;

	@Test
	void deletesActiveCouponAndSavesIt() {
		Coupon persisted = coupon(CouponStatus.ACTIVE);
		StubCouponRepository couponRepository = new StubCouponRepository(Optional.of(persisted));
		DeleteCouponUseCase useCase = new DeleteCouponUseCase(couponRepository);

		useCase.execute(ID);

		assertEquals(1, couponRepository.saved.size());
		Coupon saved = couponRepository.saved.get(0);
		assertSame(persisted, saved);
		assertEquals(CouponStatus.DELETED, saved.status());
		assertEquals(ID, saved.id());
		assertEquals(CODE, saved.code().value());
		assertEquals(DESCRIPTION, saved.description());
		assertEquals(DISCOUNT, saved.discountValue());
		assertEquals(EXPIRATION, saved.expirationDate());
		assertEquals(PUBLISHED, saved.published());
		assertEquals(REDEEMED, saved.redeemed());
	}

	@Test
	void throwsCouponNotFoundExceptionWhenIdDoesNotExist() {
		StubCouponRepository couponRepository = new StubCouponRepository(Optional.empty());
		DeleteCouponUseCase useCase = new DeleteCouponUseCase(couponRepository);

		CouponNotFoundException exception = assertThrows(
				CouponNotFoundException.class,
				() -> useCase.execute(ID));

		assertEquals("Cupom não encontrado: " + ID, exception.getMessage());
		assertEquals(0, couponRepository.saved.size());
	}

	@Test
	void throwsWhenCouponIsAlreadyDeletedAndDoesNotSave() {
		Coupon persisted = coupon(CouponStatus.DELETED);
		StubCouponRepository couponRepository = new StubCouponRepository(Optional.of(persisted));
		DeleteCouponUseCase useCase = new DeleteCouponUseCase(couponRepository);

		CouponAlreadyDeletedException exception = assertThrows(
				CouponAlreadyDeletedException.class,
				() -> useCase.execute(ID));

		assertEquals("O cupom já foi excluído", exception.getMessage());
		assertEquals(0, couponRepository.saved.size());
		assertEquals(CouponStatus.DELETED, persisted.status());
	}

	private static Coupon coupon(CouponStatus status) {
		return Coupon.reconstitute(
				ID,
				CODE,
				DESCRIPTION,
				DISCOUNT,
				EXPIRATION,
				PUBLISHED,
				REDEEMED,
				status);
	}

	private static final class StubCouponRepository implements CouponRepository {

		private final Optional<Coupon> toReturn;
		private final List<Coupon> saved = new ArrayList<>();

		private StubCouponRepository(Optional<Coupon> toReturn) {
			this.toReturn = toReturn;
		}

		@Override
		public Coupon save(Coupon coupon) {
			saved.add(coupon);
			return coupon;
		}

		@Override
		public Optional<Coupon> findById(UUID id) {
			return toReturn;
		}

		@Override
		public Optional<Coupon> findByIDAndStatusActive(UUID id) {
			return toReturn.filter(coupon -> coupon.status() == CouponStatus.ACTIVE);
		}
	}
}
