package com.picka_chew.dangcharge_Consumer.api.vehicle.dto;

import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

public record VehicleModelResponse(VehicleModel code, String label) {

	public static VehicleModelResponse from(VehicleModel model) {
		return new VehicleModelResponse(model, model.getLabel());
	}

}
