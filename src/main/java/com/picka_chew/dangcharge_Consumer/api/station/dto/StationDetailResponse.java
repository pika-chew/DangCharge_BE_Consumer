package com.picka_chew.dangcharge_Consumer.api.station.dto;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

public record StationDetailResponse(Long stationId, String name, String address, String metro, String city,
		int rapidCount, int slowCount, Set<VehicleModel> supportedModels, BigDecimal latitude, BigDecimal longitude,
		List<ChargerResponse> chargers) {

	public static StationDetailResponse from(Station station) {
		Set<VehicleModel> models = station.getSupportedModels().isEmpty()
				? EnumSet.noneOf(VehicleModel.class)
				: EnumSet.copyOf(station.getSupportedModels());
		return new StationDetailResponse(station.getId(), station.getName(), station.getAddress(), station.getMetro(),
				station.getCity(), station.getRapidCount(), station.getSlowCount(), models, station.getLatitude(),
				station.getLongitude(), station.getChargers().stream().map(ChargerResponse::from).toList());
	}

}
