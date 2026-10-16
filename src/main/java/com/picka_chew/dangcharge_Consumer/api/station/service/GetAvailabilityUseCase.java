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
 * 날짜별 예약 슬롯(30분 간격 시작, 1시간 이용)과 예약 가능 여부. 이미 예약이 걸린 슬롯은 DISABLE로 함께 내려준다.
 * 로그인 사용자의 차종을 충전소가 지원하지 않으면 빈 목록이다. 여기서 ENABLE이어도 최종 판단은 예약 생성에서 다시 한다.
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
		return ReservationTimePolicy.slots(chargers, reservations, date, now).stream()
				.map(AvailableSlotResponse::from)
				.toList();
	}

}
