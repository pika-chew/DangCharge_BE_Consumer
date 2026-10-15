package com.picka_chew.dangcharge_Consumer.api.station.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge_Consumer.api.common.dto.EventMessage;
import com.picka_chew.dangcharge_Consumer.api.station.dto.StationEventPayload;
import com.picka_chew.dangcharge_Consumer.api.station.service.ApplyStationEventUseCase;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class StationEventListenerTest {

	/** producer OutboxRelay가 보내는 메시지 형식 그대로 */
	private static final String MESSAGE = """
			{"eventId":31,"eventType":"StationUpserted","aggregateType":"station","aggregateId":5,"version":31,
			 "occurredAt":"2026-10-15T04:00:00.123456Z",
			 "payload":{"stationId":5,"name":"강남구청 충전소","address":"서울특별시 강남구 학동로 426",
			  "metro":"서울특별시","city":"강남구","rapidCount":1,"slowCount":1,"supportedModels":["TESLA","IONIQ_EV"],
			  "latitude":37.5172363,"longitude":127.0473248,"syncedAt":"2026-10-15T03:59:59Z",
			  "chargers":[
			   {"chargerId":12,"chargingType":"SLOW","maxPowerKw":7.00,"status":"AVAILABLE","dataSource":"SIMULATION"},
			   {"chargerId":11,"chargingType":"FAST","maxPowerKw":50.00,"status":"IN_USE","dataSource":"SIMULATION"}]}}
			""";

	@Mock
	ApplyStationEventUseCase applyStationEventUseCase;
	@Captor
	ArgumentCaptor<EventMessage<StationEventPayload>> captor;

	@Test
	void readsProducerMessageIntoTypedPayload() {
		new StationEventListener(applyStationEventUseCase, JsonMapper.builder().build()).onMessage(MESSAGE);

		verify(applyStationEventUseCase).execute(captor.capture());
		EventMessage<StationEventPayload> message = captor.getValue();
		assertThat(message.version()).isEqualTo(31L);
		assertThat(message.occurredAt()).isEqualTo(Instant.parse("2026-10-15T04:00:00.123456Z"));

		Station station = message.payload().toStation(message.version());
		assertThat(station.getId()).isEqualTo(5L);
		assertThat(station.getLatitude()).isEqualByComparingTo("37.5172363");
		assertThat(station.getLocation().getX()).isEqualTo(127.0473248);
		assertThat(station.getLocation().getY()).isEqualTo(37.5172363);
		assertThat(station.getSupportedModels()).containsExactlyInAnyOrder(VehicleModel.TESLA, VehicleModel.IONIQ_EV);
		assertThat(station.getEventVersion()).isEqualTo(31L);
		// 충전기는 ID 순으로 담고, 출력의 소수 자릿수를 유지한다
		assertThat(station.getChargers()).extracting("chargerId").containsExactly(11L, 12L);
		assertThat(station.getChargers().get(0).getChargingType()).isEqualTo(ChargingType.FAST);
		assertThat(station.getChargers().get(0).getOperationalStatus()).isEqualTo(OperationalStatus.IN_USE);
		assertThat(station.getChargers().get(0).getMaxPowerKw()).isEqualTo(new BigDecimal("50.00"));
	}

	@Test
	void stationWithoutCoordinatesHasNoLocation() {
		String message = MESSAGE.replace("\"latitude\":37.5172363,\"longitude\":127.0473248",
				"\"latitude\":null,\"longitude\":null");

		new StationEventListener(applyStationEventUseCase, JsonMapper.builder().build()).onMessage(message);

		verify(applyStationEventUseCase).execute(captor.capture());
		assertThat(captor.getValue().payload().toStation(1).getLocation()).isNull();
	}

	@Test
	void brokenJsonFailsWithoutCallingUseCase() {
		StationEventListener listener = new StationEventListener(applyStationEventUseCase, JsonMapper.builder().build());

		assertThatThrownBy(() -> listener.onMessage("{not json")).isInstanceOf(JacksonException.class);
		verifyNoInteractions(applyStationEventUseCase);
	}

}
