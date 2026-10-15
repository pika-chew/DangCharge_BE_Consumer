package com.picka_chew.dangcharge_Consumer.api.station.service;

import java.util.List;

import com.picka_chew.dangcharge_Consumer.api.station.dto.ChargerResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.station.adaptor.StationAdaptor;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetChargersUseCase {

	private final StationAdaptor stationAdaptor;

	public List<ChargerResponse> execute(Long stationId) {
		return stationAdaptor.getById(stationId).getChargers().stream().map(ChargerResponse::from).toList();
	}

}
