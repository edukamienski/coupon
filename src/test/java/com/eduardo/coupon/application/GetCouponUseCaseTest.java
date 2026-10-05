package com.eduardo.coupon.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.eduardo.coupon.application.exception.CouponNotFoundException;
import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;

class GetCouponUseCaseTest {

	private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final String CODE = "XYZ789";
	private static final String DESCRIPTION = "retorno do repositorio";
	private static final BigDecimal DISCOUNT = new BigDecimal("0.8");
	private static final Instant EXPIRATION = Instant.parse("2030-12-31T23:59:59Z");

	@Test
	void returnsTheExistingCoupon() {
		Coupon persisted = coupon(CouponStatus.ACTIVE);
		StubCouponRepository couponRepository = new StubCouponRepository(Optional.of(persisted));
		GetCouponUseCase useCase = new GetCouponUseCase(couponRepository);

		Coupon result = useCase.execute(ID);

		assertSame(persisted, result);
	}

	@Test
	void throwsCouponNotFoundExceptionWhenIdDoesNotExist() {
		StubCouponRepository couponRepository = new StubCouponRepository(Optional.empty());
		GetCouponUseCase useCase = new GetCouponUseCase(couponRepository);

		CouponNotFoundException exception = assertThrows(
				CouponNotFoundException.class,
				() -> useCase.execute(ID));

		assertEquals("Cupom não encontrado: " + ID, exception.getMessage());
	}

	@Test
	void throwsCouponNotFoundExceptionWhenCouponIsDeleted() {
		StubCouponRepository couponRepository = new StubCouponRepository(Optional.of(coupon(CouponStatus.DELETED)));
		GetCouponUseCase useCase = new GetCouponUseCase(couponRepository);

		CouponNotFoundException exception = assertThrows(
				CouponNotFoundException.class,
				() -> useCase.execute(ID));

		assertEquals("Cupom não encontrado: " + ID, exception.getMessage());
	}

	private static Coupon coupon(CouponStatus status) {
		return Coupon.reconstitute(
				ID,
				CODE,
				DESCRIPTION,
				DISCOUNT,
				EXPIRATION,
				false,
				false,
				status);
	}

	private static final class StubCouponRepository implements CouponRepository {

		private final Optional<Coupon> toReturn;

		private StubCouponRepository(Optional<Coupon> toReturn) {
			this.toReturn = toReturn;
		}

		@Override
		public Coupon save(Coupon coupon) {
			throw new UnsupportedOperationException();
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
