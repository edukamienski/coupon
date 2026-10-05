package com.eduardo.coupon.infrastructure.web;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.eduardo.coupon.application.port.CouponRepository;
import com.eduardo.coupon.domain.Coupon;
import com.eduardo.coupon.domain.CouponStatus;
import com.eduardo.coupon.infrastructure.CouponUseCaseConfiguration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = CouponControllerTest.WebSliceConfiguration.class)
class CouponControllerTest {

	private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID MISSING_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final Instant EXPIRATION = Instant.parse("2030-12-31T23:59:59Z");
	private static final String REQUEST_BODY = """
			{
			  "code": "ABC-123",
			  "description": "Desconto especial",
			  "discountValue": 0.8,
			  "expirationDate": "2030-12-31T23:59:59Z",
			  "published": true
			}
			""";

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private CouponRepository couponRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		((SavingCouponRepository) couponRepository).clear();
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void postCouponReturnsCreatedResponse() throws Exception {
		mockMvc.perform(post("/coupon")
				.contentType(MediaType.APPLICATION_JSON)
				.content(REQUEST_BODY))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").exists())
				.andExpect(jsonPath("$.id", not(emptyOrNullString())))
				.andExpect(jsonPath("$.code").value("ABC123"))
				.andExpect(jsonPath("$.description").value("Desconto especial"))
				.andExpect(jsonPath("$.discountValue").value(0.8))
				.andExpect(jsonPath("$.expirationDate").value("2030-12-31T23:59:59Z"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.published").value(true))
				.andExpect(jsonPath("$.redeemed").value(false));
	}

	@Test
	void postCouponRejectsInvalidJson() throws Exception {
		mockMvc.perform(post("/coupon")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postCouponRejectsMissingRequiredField() throws Exception {
		mockMvc.perform(post("/coupon")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "code": "ABC-123",
						  "discountValue": 0.8,
						  "expirationDate": "2030-12-31T23:59:59Z",
						  "published": true
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message").value("Requisição inválida"));
	}

	@Test
	void postCouponRejectsInvalidDiscount() throws Exception {
		mockMvc.perform(post("/coupon")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "code": "ABC-123",
						  "description": "Desconto especial",
						  "discountValue": 0.49,
						  "expirationDate": "2030-12-31T23:59:59Z",
						  "published": true
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("O desconto deve ser maior ou igual a 0.5"));
	}

	@Test
	void postCouponRejectsInvalidExpiration() throws Exception {
		mockMvc.perform(post("/coupon")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "code": "ABC-123",
						  "description": "Desconto especial",
						  "discountValue": 0.8,
						  "expirationDate": "2020-01-01T00:00:00Z",
						  "published": true
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("A data de expiração não pode estar no passado"));
	}

	@Test
	void postCouponTranslatesDomainException() throws Exception {
		mockMvc.perform(post("/coupon")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "code": "---",
						  "description": "Desconto especial",
						  "discountValue": 0.8,
						  "expirationDate": "2030-12-31T23:59:59Z",
						  "published": true
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(
						"O código deve ter exatamente 6 caracteres alfanuméricos após a sanitização"));
	}

	@Test
	void getCouponReturnsExistingCoupon() throws Exception {
		((SavingCouponRepository) couponRepository).seed(storedCoupon(CouponStatus.ACTIVE));

		mockMvc.perform(get("/coupon/{id}", ID))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(ID.toString()))
				.andExpect(jsonPath("$.code").value("ABC123"))
				.andExpect(jsonPath("$.description").value("Desconto especial"))
				.andExpect(jsonPath("$.discountValue").value(0.8))
				.andExpect(jsonPath("$.expirationDate").value("2030-12-31T23:59:59Z"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.published").value(true))
				.andExpect(jsonPath("$.redeemed").value(false));
	}

	@Test
	void getCouponReturnsNotFoundWhenIdDoesNotExist() throws Exception {
		mockMvc.perform(get("/coupon/{id}", MISSING_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cupom não encontrado: " + MISSING_ID));
	}

	@Test
	void getCouponReturnsNotFoundWhenUuidIsInvalid() throws Exception {
		mockMvc.perform(get("/coupon/{id}", "abc"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cupom não encontrado: abc"));
	}

	@Test
	void deleteCouponReturnsNoContent() throws Exception {
		((SavingCouponRepository) couponRepository).seed(storedCoupon(CouponStatus.ACTIVE));

		mockMvc.perform(delete("/coupon/{id}", ID))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));
	}

	@Test
	void deleteCouponReturnsNotFoundWhenIdDoesNotExist() throws Exception {
		mockMvc.perform(delete("/coupon/{id}", MISSING_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cupom não encontrado: " + MISSING_ID));
	}

	@Test
	void deleteCouponReturnsConflictWhenCouponIsAlreadyDeleted() throws Exception {
		((SavingCouponRepository) couponRepository).seed(storedCoupon(CouponStatus.DELETED));

		mockMvc.perform(delete("/coupon/{id}", ID))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("O cupom já foi excluído"));
	}

	@Test
	void deleteCouponReturnsNotFoundWhenUuidIsInvalid() throws Exception {
		mockMvc.perform(delete("/coupon/{id}", "abc"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cupom não encontrado: abc"));
	}

	private static Coupon storedCoupon(CouponStatus status) {
		return Coupon.reconstitute(
				ID,
				"ABC123",
				"Desconto especial",
				new BigDecimal("0.8"),
				EXPIRATION,
				true,
				false,
				status);
	}

	@Configuration
	@EnableWebMvc
	@Import({
			CouponController.class,
			CouponExceptionHandler.class,
			CouponUseCaseConfiguration.class,
			CouponRepositoryTestConfiguration.class
	})
	static class WebSliceConfiguration {
	}

	@TestConfiguration
	static class CouponRepositoryTestConfiguration {

		@Bean
		CouponRepository couponRepository() {
			return new SavingCouponRepository();
		}
	}

	private static final class SavingCouponRepository implements CouponRepository {

		private final Map<UUID, Coupon> coupons = new HashMap<>();

		private void clear() {
			coupons.clear();
		}

		private void seed(Coupon coupon) {
			coupons.put(coupon.id(), coupon);
		}

		@Override
		public Coupon save(Coupon coupon) {
			coupons.put(coupon.id(), coupon);
			return coupon;
		}

		@Override
		public Optional<Coupon> findById(UUID id) {
			return Optional.ofNullable(coupons.get(id));
		}
	}
}
