package com.picka_chew.dangcharge_Consumer.api.station.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.picka_chew.dangcharge_Consumer.domain.station.entity.Charger;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.ChargerDataSource;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

/** producer StationUpsertedEvent 본문. 소속 충전기 전체가 함께 온다 */
public record StationEventPayload(Long stationId, String name, String address, String metro, String city,
		int rapidCount, int slowCount, Set<VehicleModel> supportedModels, BigDecimal latitude, BigDecimal longitude,
		Instant syncedAt, List<ChargerPayload> chargers) {

	public Station toStation(long eventVersion) {
		return Station.builder()
				.id(stationId)
				.name(name)
				.address(address)
				.metro(metro)
				.city(city)
				.rapidCount(rapidCount)
				.slowCount(slowCount)
				.supportedModels(supportedModels == null ? new HashSet<>() : new HashSet<>(supportedModels))
				.latitude(latitude)
				.longitude(longitude)
				.location(Station.toLocation(latitude, longitude))
				.syncedAt(syncedAt)
				.chargers(chargers == null ? List.of() : chargers.stream()
						.sorted(Comparator.comparing(ChargerPayload::chargerId))
						.map(ChargerPayload::toCharger)
						.toList())
				.eventVersion(eventVersion)
				.build();
	}

	public record ChargerPayload(Long chargerId, ChargingType chargingType, BigDecimal maxPowerKw,
			OperationalStatus status, ChargerDataSource dataSource) {

		Charger toCharger() {
			return Charger.builder()
					.chargerId(chargerId)
					.chargingType(chargingType)
					.maxPowerKw(maxPowerKw)
					.operationalStatus(status == null ? OperationalStatus.UNKNOWN : status)
					.dataSource(dataSource)
					.build();
		}

	}

}
