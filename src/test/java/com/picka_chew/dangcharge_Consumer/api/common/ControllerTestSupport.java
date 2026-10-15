package com.picka_chew.dangcharge_Consumer.api.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.picka_chew.dangcharge_Consumer.api.charging.service.GetChargingHistoriesUseCase;
import com.picka_chew.dangcharge_Consumer.api.charging.service.GetChargingHistoryUseCase;
import com.picka_chew.dangcharge_Consumer.api.reservation.service.GetMyReservationUseCase;
import com.picka_chew.dangcharge_Consumer.api.reservation.service.GetReservationUseCase;
import com.picka_chew.dangcharge_Consumer.api.station.service.GetAvailabilityUseCase;
import com.picka_chew.dangcharge_Consumer.api.station.service.GetChargersUseCase;
import com.picka_chew.dangcharge_Consumer.api.station.service.GetNearbyStationsUseCase;
import com.picka_chew.dangcharge_Consumer.api.station.service.GetStationUseCase;
import com.picka_chew.dangcharge_Consumer.api.station.service.SearchStationsUseCase;
import com.picka_chew.dangcharge_Consumer.api.user.service.GetMyInfoUseCase;
import com.picka_chew.dangcharge_Consumer.api.vehicle.service.GetVehicleModelsUseCase;
import com.picka_chew.dangcharge_Consumer.common.config.TimeConfig;
import com.picka_chew.dangcharge_Consumer.security.TestJwtIssuer;
import com.picka_chew.dangcharge_Consumer.security.config.JwtConfig;
import com.picka_chew.dangcharge_Consumer.security.config.SecurityConfig;

/**
 * 컨트롤러 슬라이스 테스트 공통 설정. 실제 보안 설정에 테스트 키의 공개키를 넣고, UseCase는 목으로 바꾼다.
 */
@WebMvcTest
@Import({SecurityConfig.class, JwtConfig.class, TimeConfig.class})
public abstract class ControllerTestSupport {

	protected static final TestJwtIssuer TOKENS = new TestJwtIssuer();

	@Autowired
	protected MockMvc mockMvc;

	@MockitoBean
	protected GetMyInfoUseCase getMyInfoUseCase;
	@MockitoBean
	protected GetVehicleModelsUseCase getVehicleModelsUseCase;
	@MockitoBean
	protected GetNearbyStationsUseCase getNearbyStationsUseCase;
	@MockitoBean
	protected SearchStationsUseCase searchStationsUseCase;
	@MockitoBean
	protected GetStationUseCase getStationUseCase;
	@MockitoBean
	protected GetChargersUseCase getChargersUseCase;
	@MockitoBean
	protected GetAvailabilityUseCase getAvailabilityUseCase;
	@MockitoBean
	protected GetMyReservationUseCase getMyReservationUseCase;
	@MockitoBean
	protected GetReservationUseCase getReservationUseCase;
	@MockitoBean
	protected GetChargingHistoriesUseCase getChargingHistoriesUseCase;
	@MockitoBean
	protected GetChargingHistoryUseCase getChargingHistoryUseCase;

	@DynamicPropertySource
	static void jwtPublicKey(DynamicPropertyRegistry registry) {
		registry.add("dangcharge.jwt.public-key", TOKENS::publicKeyBase64);
	}

	protected String bearer(Long userId) {
		return TOKENS.bearer(userId);
	}

}
