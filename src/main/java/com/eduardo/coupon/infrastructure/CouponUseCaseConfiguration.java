package com.eduardo.coupon.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eduardo.coupon.application.CreateCouponUseCase;
import com.eduardo.coupon.application.DeleteCouponUseCase;
import com.eduardo.coupon.application.GetCouponUseCase;
import com.eduardo.coupon.application.port.CouponRepository;

@Configuration
public class CouponUseCaseConfiguration {

	@Bean
	CreateCouponUseCase createCouponUseCase(CouponRepository couponRepository) {
		return new CreateCouponUseCase(couponRepository);
	}

	@Bean
	GetCouponUseCase getCouponUseCase(CouponRepository couponRepository) {
		return new GetCouponUseCase(couponRepository);
	}

	@Bean
	DeleteCouponUseCase deleteCouponUseCase(CouponRepository couponRepository) {
		return new DeleteCouponUseCase(couponRepository);
	}
}
