package com.picka_chew.dangcharge_Consumer.api.charging.service;

import com.picka_chew.dangcharge_Consumer.api.charging.dto.ChargingHistoryResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.reservation.adaptor.ReservationAdaptor;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetChargingHistoryUseCase {

	private final ReservationAdaptor reservationAdaptor;

	public ChargingHistoryResponse execute(Long userId, Long reservationId) {
		return ChargingHistoryResponse.from(reservationAdaptor.getCompletedByIdAndUserId(reservationId, userId));
	}

}
