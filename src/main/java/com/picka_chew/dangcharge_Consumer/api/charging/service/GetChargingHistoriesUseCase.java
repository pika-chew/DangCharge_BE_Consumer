package com.picka_chew.dangcharge_Consumer.api.charging.service;

import org.springframework.data.domain.PageRequest;
import com.picka_chew.dangcharge_Consumer.api.charging.dto.ChargingHistoryResponse;
import com.picka_chew.dangcharge_Consumer.api.common.dto.PagedResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.reservation.adaptor.ReservationAdaptor;

import lombok.RequiredArgsConstructor;

/** 충전을 마친 예약을 최근 완료 순으로 */
@UseCase
@RequiredArgsConstructor
public class GetChargingHistoriesUseCase {

	private final ReservationAdaptor reservationAdaptor;

	public PagedResponse<ChargingHistoryResponse> execute(Long userId, int page, int size) {
		return PagedResponse.of(reservationAdaptor.findCompletedByUserId(userId, PageRequest.of(page, size)),
				ChargingHistoryResponse::from);
	}

}
