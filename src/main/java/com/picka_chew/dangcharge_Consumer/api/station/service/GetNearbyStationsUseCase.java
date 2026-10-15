package com.picka_chew.dangcharge_Consumer.api.station.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.picka_chew.dangcharge_Consumer.api.station.dto.StationMarkerResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.common.exception.GeneralException;
import com.picka_chew.dangcharge_Consumer.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Charger;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.station.policy.GeoDistancePolicy;

import lombok.RequiredArgsConstructor;

/**
 * 지도 반경 안의 충전소. MongoDB 구면 원 조회($geoWithin)로 먼저 거른 뒤 producer와 같은 하버사인 거리로 다시 거른다.
 * 좌표가 없는 충전소는 나오지 않는다.
 */
@UseCase
@RequiredArgsConstructor
public class GetNearbyStationsUseCase {

	static final double MAX_RADIUS_METERS = 20_000;
	private static final int MAX_RESULTS = 200;

	private final StationAdaptor stationAdaptor;

	public List<StationMarkerResponse> execute(BigDecimal lat, BigDecimal lng, BigDecimal radius,
			BigDecimal minKw, boolean availableOnly) {
		double latitude = lat.doubleValue();
		double longitude = lng.doubleValue();
		double radiusMeters = radius.doubleValue();
		if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
			throw new GeneralException(ErrorStatus.BAD_REQUEST);
		}
		if (radiusMeters <= 0 || radiusMeters > MAX_RADIUS_METERS) {
			throw new GeneralException(ErrorStatus.INVALID_RADIUS);
		}

		boolean filtersChargers = minKw != null || availableOnly;
		List<StationMarkerResponse> markers = new ArrayList<>();
		for (Station station : stationAdaptor.findAllWithin(latitude, longitude, radiusMeters)) {
			long distance = Math.round(GeoDistancePolicy.distanceMeters(latitude, longitude,
					station.getLatitude().doubleValue(), station.getLongitude().doubleValue()));
			if (distance > radiusMeters) {
				continue;
			}
			List<Charger> chargers = station.getChargers();
			if (filtersChargers && chargers.stream()
					.noneMatch(charger -> matches(charger, minKw, availableOnly))) {
				continue;
			}
			int available = (int) chargers.stream()
					.filter(charger -> charger.getOperationalStatus() == OperationalStatus.AVAILABLE).count();
			markers.add(StationMarkerResponse.of(station, available, distance));
		}
		markers.sort(Comparator.comparingLong(StationMarkerResponse::distanceMeters));
		return markers.size() > MAX_RESULTS ? markers.subList(0, MAX_RESULTS) : markers;
	}

	private static boolean matches(Charger charger, BigDecimal minKw, boolean availableOnly) {
		if (minKw != null && (charger.getMaxPowerKw() == null || charger.getMaxPowerKw().compareTo(minKw) < 0)) {
			return false;
		}
		return !availableOnly || charger.getOperationalStatus() == OperationalStatus.AVAILABLE;
	}

}
