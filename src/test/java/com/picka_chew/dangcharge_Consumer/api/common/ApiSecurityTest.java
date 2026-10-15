package com.picka_chew.dangcharge_Consumer.api.common;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;

import com.picka_chew.dangcharge_Consumer.api.reservation.dto.MyReservationResponse;
import com.picka_chew.dangcharge_Consumer.api.user.dto.UserResponse;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.common.exception.GeneralException;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;
import com.picka_chew.dangcharge_Consumer.security.TestJwtIssuer;

/** 인증이 필요한 API와 필요 없는 API가 producer와 같게 나뉘는지, producer 토큰을 검증하는지, 오류가 공통 포맷인지 확인한다 */
class ApiSecurityTest extends ControllerTestSupport {

	@ParameterizedTest
	@CsvSource({
			"/api/v1/vehicle-models",
			"/api/v1/stations?lat=37.5&lng=127.0&radius=1000",
			"/api/v1/stations/search?keyword=강남",
			"/api/v1/stations/1",
			"/api/v1/stations/1/chargers"
	})
	void publicEndpointsDoNotNeedLogin(String uri) throws Exception {
		mockMvc.perform(get(uri)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
	}

	@ParameterizedTest
	@CsvSource({
			"/api/v1/users/me",
			"/api/v1/stations/1/availability?date=2026-10-14",
			"/api/v1/reservations/me",
			"/api/v1/reservations/1",
			"/api/v1/charging/history",
			"/api/v1/charging/history/1"
	})
	void protectedEndpointsRejectMissingToken(String uri) throws Exception {
		mockMvc.perform(get(uri))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	@Test
	void producerAccessTokenIdentifiesUser() throws Exception {
		given(getMyInfoUseCase.execute(7L)).willReturn(new UserResponse(7L, "driver", "운전자", VehicleModel.TESLA));

		mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.userId").value(7))
				.andExpect(jsonPath("$.data.loginId").value("driver"))
				.andExpect(jsonPath("$.data.vehicleModel").value("TESLA"));
	}

	@Test
	void rejectsRefreshTokenWrongIssuerAndForeignKey() throws Exception {
		String refresh = TOKENS.issue(7L, "refresh", "dangcharge");
		String otherIssuer = TOKENS.issue(7L, "access", "someone-else");
		String otherKey = new TestJwtIssuer().issue(7L, "access", "dangcharge");

		for (String token : new String[] {refresh, otherIssuer, otherKey}) {
			mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
		}
		verifyNoInteractions(getMyInfoUseCase);
	}

	@Test
	void writeApisDoNotExistOnConsumer() throws Exception {
		mockMvc.perform(post("/api/v1/reservations").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false));
		mockMvc.perform(post("/api/v1/charging/1/stop").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void noActiveReservationIsOkWithNoneState() throws Exception {
		given(getMyReservationUseCase.execute(7L)).willReturn(MyReservationResponse.none());

		mockMvc.perform(get("/api/v1/reservations/me").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.state").value("NONE"))
				.andExpect(jsonPath("$.data.message").value("진행 중인 예약이 없습니다"))
				.andExpect(jsonPath("$.data.reservation").doesNotExist());
	}

	@Test
	void domainErrorUsesCommonFormat() throws Exception {
		given(getStationUseCase.execute(99L)).willThrow(new GeneralException(ErrorStatus.STATION_NOT_FOUND));

		mockMvc.perform(get("/api/v1/stations/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("STATION_NOT_FOUND"))
				.andExpect(jsonPath("$.error.message").value("존재하지 않는 충전소입니다"));
	}

	@Test
	void invalidParametersAreBadRequestInCommonFormat() throws Exception {
		mockMvc.perform(get("/api/v1/charging/history?size=101").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
		mockMvc.perform(get("/api/v1/stations?lat=37.5&lng=127.0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
		verifyNoInteractions(getChargingHistoriesUseCase, getNearbyStationsUseCase);
	}

}
