package com.picka_chew.dangcharge_Consumer.api.station.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge_Consumer.api.station.dto.AvailableSlotResponse;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.model.SlotStatus;
import com.picka_chew.dangcharge_Consumer.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Charger;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.user.adaptor.UserAdaptor;
import com.picka_chew.dangcharge_Consumer.domain.user.entity.User;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

@ExtendWith(MockitoExtension.class)
class GetAvailabilityUseCaseTest {

	private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

	@Mock
	StationAdaptor stationAdaptor;
	@Mock
	ReservationAdaptor reservationAdaptor;
	@Mock
	UserAdaptor userAdaptor;
	GetAvailabilityUseCase useCase;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(kst("2026-10-15T09:10"), ZoneOffset.UTC);
		useCase = new GetAvailabilityUseCase(stationAdaptor, reservationAdaptor, userAdaptor, clock);
	}

	@Test
	void listsSlotsAfterNowWithReservedOnesDisabled() {
		given(stationAdaptor.getById(5L)).willReturn(station(Set.of(VehicleModel.TESLA)));
		given(userAdaptor.getById(7L)).willReturn(User.builder().id(7L).vehicleModel(VehicleModel.TESLA).build());
		given(reservationAdaptor.findConfirmedOverlapping(eq(List.of(11L, 12L)), any(), any())).willReturn(List.of(
				Reservation.builder().chargerId(11L).startsAt(kst("2026-10-15T12:00")).endsAt(kst("2026-10-15T13:00"))
						.build()));

		List<AvailableSlotResponse> slots = useCase.execute(7L, 5L, DATE);

		// 고장 난 12번은 빠지고, 11번은 09:30부터 23:30까지 30분 간격 29개가 나온다
		assertThat(slots).allMatch(slot -> slot.chargerId().equals(11L));
		assertThat(slots).hasSize(29);
		assertThat(slots.get(0).startAt()).isEqualTo(OffsetDateTime.parse("2026-10-15T09:30:00+09:00"));
		// 12:00 예약과 겹치는 3개만 DISABLE이다
		assertThat(slots).filteredOn(slot -> slot.status() == SlotStatus.DISABLE)
				.extracting(AvailableSlotResponse::startAt)
				.containsExactly(OffsetDateTime.parse("2026-10-15T11:30:00+09:00"),
						OffsetDateTime.parse("2026-10-15T12:00:00+09:00"),
						OffsetDateTime.parse("2026-10-15T12:30:00+09:00"));
		assertThat(slots).filteredOn(slot -> slot.status() == SlotStatus.ENABLE).hasSize(26);
	}

	@Test
	void unsupportedVehicleSeesNoSlots() {
		given(stationAdaptor.getById(5L)).willReturn(station(Set.of(VehicleModel.IONIQ_EV)));
		given(userAdaptor.getById(7L)).willReturn(User.builder().id(7L).vehicleModel(VehicleModel.TESLA).build());

		assertThat(useCase.execute(7L, 5L, DATE)).isEmpty();
		verifyNoInteractions(reservationAdaptor);
	}

	@Test
	void rejectsDatesOutsideBookingWindow() {
		assertThatThrownBy(() -> useCase.execute(7L, 5L, DATE.minusDays(1)))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_AVAILABILITY_DATE);
		assertThatThrownBy(() -> useCase.execute(7L, 5L, DATE.plusDays(8)))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_AVAILABILITY_DATE);
		verifyNoInteractions(stationAdaptor);
	}

	private static Station station(Set<VehicleModel> models) {
		return Station.builder().id(5L).supportedModels(models).chargers(List.of(
				Charger.builder().chargerId(11L).operationalStatus(OperationalStatus.AVAILABLE).build(),
				Charger.builder().chargerId(12L).operationalStatus(OperationalStatus.OUT_OF_SERVICE).build())).build();
	}

	private static Instant kst(String localDateTime) {
		return OffsetDateTime.parse(localDateTime + ":00+09:00").toInstant();
	}

}
