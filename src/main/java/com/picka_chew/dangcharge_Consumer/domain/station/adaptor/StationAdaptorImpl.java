package com.picka_chew.dangcharge_Consumer.domain.station.adaptor;

import java.util.List;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.Sphere;

import com.picka_chew.dangcharge_Consumer.common.annotation.Adaptor;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.common.exception.GeneralException;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.station.policy.GeoDistancePolicy;
import com.picka_chew.dangcharge_Consumer.domain.station.repository.StationRepository;

import lombok.RequiredArgsConstructor;

@Adaptor
@RequiredArgsConstructor
public class StationAdaptorImpl implements StationAdaptor {

	/** 반경 판단은 반올림한 거리로 하므로 경계에서 1m 넉넉히 가져온다 */
	private static final double BOUNDARY_MARGIN_METERS = 1;

	private final StationRepository stationRepository;

	@Override
	public Station getById(Long stationId) {
		return stationRepository.findById(stationId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.STATION_NOT_FOUND));
	}

	@Override
	public List<Station> search(String keyword, String metro, String city, int limit) {
		return stationRepository.search(keyword, metro, city, limit);
	}

	@Override
	public List<Station> findAllWithin(double latitude, double longitude, double radiusMeters) {
		// Distance의 기본 단위(NEUTRAL)를 그대로 라디안으로 넘긴다
		return stationRepository.findAllByLocationWithin(new Sphere(new Point(longitude, latitude),
				new Distance(GeoDistancePolicy.toRadians(radiusMeters + BOUNDARY_MARGIN_METERS))));
	}

}
