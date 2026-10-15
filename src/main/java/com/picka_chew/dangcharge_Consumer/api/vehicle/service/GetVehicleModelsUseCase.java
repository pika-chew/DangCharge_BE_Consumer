package com.picka_chew.dangcharge_Consumer.api.vehicle.service;

import java.util.Arrays;
import java.util.List;

import com.picka_chew.dangcharge_Consumer.api.vehicle.dto.VehicleModelResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

@UseCase
public class GetVehicleModelsUseCase {

	public List<VehicleModelResponse> execute() {
		return Arrays.stream(VehicleModel.values()).map(VehicleModelResponse::from).toList();
	}

}
