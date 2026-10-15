package com.picka_chew.dangcharge_Consumer.api.station.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge_Consumer.api.station.dto.StationMarkerResponse;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Charger;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;

@ExtendWith(MockitoExtension.class)
class GetNearbyStationsUseCaseTest {

	private static final BigDecimal LAT = new BigDecimal("37.5000000");
	private static final BigDecimal LNG = new BigDecimal("127.0000000");

	@Mock
	StationAdaptor stationAdaptor;
	@InjectMocks
	GetNearbyStationsUseCase useCase;

	@Test
	void keepsStationsInsideRadiusSortedByDistance() {
		Station far = station(1L, "37.5080000", List.of());
		Station near = station(2L, "37.5010000", List.of(charger(10L, OperationalStatus.AVAILABLE, "50.00")));
		Station outside = station(3L, "37.5100000", List.of());
		given(stationAdaptor.findAllWithin(anyDouble(), anyDouble(), anyDouble())).willReturn(List.of(far, near, outside));

		List<StationMarkerResponse> markers = useCase.execute(LAT, LNG, new BigDecimal("1000"), null, false);

		assertThat(markers).extracting(StationMarkerResponse::stationId).containsExactly(2L, 1L);
		assertThat(markers.get(0).availableChargerCount()).isEqualTo(1);
		assertThat(markers.get(0).distanceMeters()).isBetween(100L, 120L);
	}

	@Test
	void availableOnlyAndMinKwFilterByChargers() {
		Station fastAvailable = station(1L, "37.5010000", List.of(charger(10L, OperationalStatus.AVAILABLE, "50.00")));
		Station slowAvailable = station(2L, "37.5020000", List.of(charger(11L, OperationalStatus.AVAILABLE, "7.00")));
		Station busy = station(3L, "37.5030000", List.of(charger(12L, OperationalStatus.IN_USE, "50.00")));
		given(stationAdaptor.findAllWithin(anyDouble(), anyDouble(), anyDouble()))
				.willReturn(List.of(fastAvailable, slowAvailable, busy));

		assertThat(useCase.execute(LAT, LNG, new BigDecimal("1000"), null, true))
				.extracting(StationMarkerResponse::stationId).containsExactly(1L, 2L);
		assertThat(useCase.execute(LAT, LNG, new BigDecimal("1000"), new BigDecimal("50"), false))
				.extracting(StationMarkerResponse::stationId).containsExactly(1L, 3L);
	}

	@Test
	void rejectsRadiusOutOfRange() {
		assertThatThrownBy(() -> useCase.execute(LAT, LNG, new BigDecimal("20001"), null, false))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RADIUS);
		assertThatThrownBy(() -> useCase.execute(LAT, LNG, BigDecimal.ZERO, null, false))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RADIUS);
		verifyNoInteractions(stationAdaptor);
	}

	private static Station station(Long id, String latitude, List<Charger> chargers) {
		return Station.builder().id(id).name("충전소" + id).latitude(new BigDecimal(latitude)).longitude(LNG)
				.chargers(chargers).build();
	}

	private static Charger charger(Long id, OperationalStatus status, String kw) {
		return Charger.builder().chargerId(id).operationalStatus(status).maxPowerKw(new BigDecimal(kw)).build();
	}

}
