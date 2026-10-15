package com.picka_chew.dangcharge_Consumer.api.station.service;

import com.picka_chew.dangcharge_Consumer.api.station.dto.StationDetailResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.station.adaptor.StationAdaptor;

import lombok.RequiredArgsConstructor;

/** 충전소 정보와 소속 충전기 전체를 함께 내려준다 */
@UseCase
@RequiredArgsConstructor
public class GetStationUseCase {

	private final StationAdaptor stationAdaptor;

	public StationDetailResponse execute(Long stationId) {
		return StationDetailResponse.from(stationAdaptor.getById(stationId));
	}

}
