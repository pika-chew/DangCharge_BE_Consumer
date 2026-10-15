package com.picka_chew.dangcharge_Consumer.api.station.dto;

import java.math.BigDecimal;

import com.picka_chew.dangcharge_Consumer.domain.station.entity.Charger;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.OperationalStatus;

public record ChargerResponse(Long chargerId, ChargingType chargingType, BigDecimal maxPowerKw,
		OperationalStatus status) {

	public static ChargerResponse from(Charger charger) {
		return new ChargerResponse(charger.getChargerId(), charger.getChargingType(), charger.getMaxPowerKw(),
				charger.getOperationalStatus());
	}

}
