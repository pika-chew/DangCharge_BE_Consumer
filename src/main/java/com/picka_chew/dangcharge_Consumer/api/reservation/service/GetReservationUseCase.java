package com.picka_chew.dangcharge_Consumer.api.reservation.service;

import com.picka_chew.dangcharge_Consumer.api.reservation.dto.ReservationDetailResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.reservation.adaptor.ReservationAdaptor;

import lombok.RequiredArgsConstructor;

/** 내 예약이면 상태(예약·취소·완료 등)와 관계없이 조회한다. 남의 예약이면 RESERVATION_NOT_FOUND */
@UseCase
@RequiredArgsConstructor
public class GetReservationUseCase {

	private final ReservationAdaptor reservationAdaptor;

	public ReservationDetailResponse execute(Long userId, Long reservationId) {
		return ReservationDetailResponse.from(reservationAdaptor.getByIdAndUserId(reservationId, userId));
	}

}
