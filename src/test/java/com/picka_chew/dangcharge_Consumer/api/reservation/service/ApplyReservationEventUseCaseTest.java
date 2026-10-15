package com.picka_chew.dangcharge_Consumer.api.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge_Consumer.api.common.dto.EventMessage;
import com.picka_chew.dangcharge_Consumer.api.reservation.dto.ReservationEventPayload;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge_Consumer.domain.reservation.service.ReservationDomainService;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

@ExtendWith(MockitoExtension.class)
class ApplyReservationEventUseCaseTest {

	private static final Instant OCCURRED_AT = Instant.parse("2026-10-15T01:00:00Z");

	@Mock
	ReservationDomainService reservationDomainService;
	@InjectMocks
	ApplyReservationEventUseCase useCase;
	@Captor
	ArgumentCaptor<Reservation> captor;

	@Test
	void createdEventRecordsOccurredAtAsCreationTime() {
		given(reservationDomainService.apply(any())).willReturn(true);

		useCase.execute(message("ReservationCreated", ReservationStatus.CONFIRMED, 10));

		verify(reservationDomainService).apply(captor.capture());
		Reservation reservation = captor.getValue();
		assertThat(reservation.getCreatedAt()).isEqualTo(OCCURRED_AT);
		assertThat(reservation.getEventVersion()).isEqualTo(10L);
		assertThat(reservation.getUserId()).isEqualTo(7L);
		assertThat(reservation.getStationName()).isEqualTo("강남구청 충전소");
	}

	@Test
	void laterEventsLeaveCreationTimeToPreviousDocument() {
		given(reservationDomainService.apply(any())).willReturn(false);

		useCase.execute(message("ChargingStarted", ReservationStatus.CHARGING, 11));

		verify(reservationDomainService).apply(captor.capture());
		assertThat(captor.getValue().getCreatedAt()).isNull();
		assertThat(captor.getValue().getStatus()).isEqualTo(ReservationStatus.CHARGING);
	}

	@Test
	void unknownEventTypeIsSkipped() {
		useCase.execute(message("ReservationMoved", ReservationStatus.CONFIRMED, 12));

		verifyNoInteractions(reservationDomainService);
	}

	private static EventMessage<ReservationEventPayload> message(String eventType, ReservationStatus status,
			long version) {
		ReservationEventPayload payload = new ReservationEventPayload(1L, 7L, 11L, 5L, "강남구청 충전소",
				"서울특별시 강남구 학동로 426", VehicleModel.TESLA, Instant.parse("2026-10-15T03:00:00Z"),
				Instant.parse("2026-10-15T04:00:00Z"), status, null, null);
		return new EventMessage<>(version, eventType, "reservation", 1L, version, OCCURRED_AT, payload);
	}

}
