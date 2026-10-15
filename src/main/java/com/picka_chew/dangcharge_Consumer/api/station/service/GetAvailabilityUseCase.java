package com.picka_chew.dangcharge_Consumer.api.station.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.picka_chew.dangcharge_Consumer.api.station.dto.AvailableSlotResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.common.exception.GeneralException;
import com.picka_chew.dangcharge_Consumer.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.policy.ReservationTimePolicy;
import com.picka_chew.dangcharge_Consumer.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Charger;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.user.adaptor.UserAdaptor;
import com.picka_chew.dangcharge_Consumer.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * 날짜별로 지금 예약할 수 있는 시작 시각(30분 간격, 1시간 이용). 로그인 사용자의 차종을 충전소가 지원하지 않으면 빈 목록이다.
 * 여기서 비어 있다고 나와도 최종 판단은 예약 생성에서 다시 한다.
 */
@UseCase
@RequiredArgsConstructor
public class GetAvailabilityUseCase {

	private final StationAdaptor stationAdaptor;
	private final ReservationAdaptor reservationAdaptor;
	private final UserAdaptor userAdaptor;
	private final Clock clock;

	public List<AvailableSlotResponse> execute(Long userId, Long stationId, LocalDate date) {
		Instant now = Instant.now(clock);
		if (!ReservationTimePolicy.isBookableDate(date, now)) {
			throw new GeneralException(ErrorStatus.INVALID_AVAILABILITY_DATE);
		}
		Station station = stationAdaptor.getById(stationId);
		User user = userAdaptor.getById(userId);
		if (!station.supports(user.getVehicleModel())) {
			return List.of();
		}

		List<Charger> chargers = station.getChargers();
		List<Reservation> reservations = reservationAdaptor.findConfirmedOverlapping(
				chargers.stream().map(Charger::getChargerId).toList(),
				ReservationTimePolicy.startOfDay(date),
				ReservationTimePolicy.endOf(ReservationTimePolicy.startOfDay(date.plusDays(1))));
		return ReservationTimePolicy.availableSlots(chargers, reservations, date, now).stream()
				.map(AvailableSlotResponse::from)
				.toList();
	}

}
