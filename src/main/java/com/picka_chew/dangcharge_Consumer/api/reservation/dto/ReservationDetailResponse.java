package com.picka_chew.dangcharge_Consumer.api.reservation.dto;

import java.time.OffsetDateTime;

import com.picka_chew.dangcharge_Consumer.common.util.TimeUtils;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.ReservationStatus;

/**
 * 예약 상세확인하기 화면. producer의 예약 생성 응답과 같은 형식이다.
 *
 * @param reservationNumber 사용자에게 보여주는 예약 번호. 예: DC20261015-000001
 */
public record ReservationDetailResponse(Long reservationId, String reservationNumber, OffsetDateTime startAt,
		OffsetDateTime endAt, Long stationId, String stationName, String stationAddress, Long chargerId,
		ReservationStatus status) {

	public static ReservationDetailResponse from(Reservation reservation) {
		return new ReservationDetailResponse(reservation.getId(), reservation.getReservationNumber(),
				TimeUtils.toOffsetDateTime(reservation.getStartsAt()), TimeUtils.toOffsetDateTime(reservation.getEndsAt()),
				reservation.getStationId(), reservation.getStationName(), reservation.getStationAddress(),
				reservation.getChargerId(), reservation.getStatus());
	}

}
