package com.picka_chew.dangcharge_Consumer.api.reservation.service;

import java.time.Clock;
import java.time.Instant;

import com.picka_chew.dangcharge_Consumer.api.reservation.dto.MyReservationResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.reservation.adaptor.ReservationAdaptor;

import lombok.RequiredArgsConstructor;

/** 사용자당 진행 중인 예약은 하나라서 ID 없이 조회한다 */
@UseCase
@RequiredArgsConstructor
public class GetMyReservationUseCase {

	private final ReservationAdaptor reservationAdaptor;
	private final Clock clock;

	public MyReservationResponse execute(Long userId) {
		return reservationAdaptor.findActiveByUserId(userId, Instant.now(clock))
				.map(MyReservationResponse::from)
				.orElseGet(MyReservationResponse::none);
	}

}
