package com.eduardo.coupon.infrastructure.web;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.eduardo.coupon.application.exception.CouponNotFoundException;
import com.eduardo.coupon.domain.exception.CouponAlreadyDeletedException;
import com.eduardo.coupon.domain.exception.InvalidCouponCodeException;
import com.eduardo.coupon.domain.exception.InvalidDiscountException;
import com.eduardo.coupon.domain.exception.InvalidExpirationException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = CouponExceptionHandlerTest.WebSliceConfiguration.class)
class CouponExceptionHandlerTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void shouldReturnBadRequestWhenCouponCodeIsInvalid() throws Exception {
		expectStatus("/errors/invalid-code", 400);
	}

	@Test
	void shouldReturnBadRequestWhenDiscountIsInvalid() throws Exception {
		expectStatus("/errors/invalid-discount", 400);
	}

	@Test
	void shouldReturnBadRequestWhenExpirationIsInvalid() throws Exception {
		expectStatus("/errors/invalid-expiration", 400);
	}

	@Test
	void shouldReturnConflictWhenCouponIsAlreadyDeleted() throws Exception {
		expectStatus("/errors/already-deleted", 409);
	}

	@Test
	void shouldReturnNotFoundWhenCouponDoesNotExist() throws Exception {
		expectStatus("/errors/not-found", 404);
	}

	@Test
	void shouldReturnBadRequestWhenRequestValidationFails() throws Exception {
		mockMvc.perform(post("/errors/validation")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message", not(emptyOrNullString())));
	}

	@Test
	void shouldReturnBadRequestWhenRequestBodyIsMalformed() throws Exception {
		mockMvc.perform(post("/errors/malformed")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message", not(emptyOrNullString())));
	}

	@Test
	void shouldReturnInternalServerErrorForUnexpectedException() throws Exception {
		mockMvc.perform(get("/errors/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message").value("Erro interno"));
	}

	private void expectStatus(String path, int status) throws Exception {
		mockMvc.perform(get(path))
				.andExpect(status().is(status))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message", not(emptyOrNullString())));
	}

	@Configuration
	@EnableWebMvc
	@Import({CouponExceptionHandler.class, ErrorTriggerController.class})
	static class WebSliceConfiguration {
	}

	@RestController
	@RequestMapping("/errors")
	static class ErrorTriggerController {

		@GetMapping("/invalid-code")
		void invalidCode() {
			throw new InvalidCouponCodeException();
		}

		@GetMapping("/invalid-discount")
		void invalidDiscount() {
			throw new InvalidDiscountException();
		}

		@GetMapping("/invalid-expiration")
		void invalidExpiration() {
			throw new InvalidExpirationException();
		}

		@GetMapping("/already-deleted")
		void alreadyDeleted() {
			throw new CouponAlreadyDeletedException();
		}

		@GetMapping("/not-found")
		void notFound() {
			throw new CouponNotFoundException("11111111-1111-1111-1111-111111111111");
		}

		@PostMapping("/validation")
		void validation(@Valid @RequestBody RequiredField body) {
		}

		@PostMapping("/malformed")
		void malformed(@RequestBody RequiredField body) {
		}

		@GetMapping("/unexpected")
		void unexpected() {
			throw new IllegalStateException("falha inesperada");
		}
	}

	record RequiredField(@NotBlank String description) {
	}
}
