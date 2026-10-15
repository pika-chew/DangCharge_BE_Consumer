package com.picka_chew.dangcharge_Consumer.domain.station.adaptor;

import java.util.List;

import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;

public interface StationAdaptor {

	/** 소속 충전기까지 들어 있다. 없으면 STATION_NOT_FOUND */
	Station getById(Long stationId);

	List<Station> search(String keyword, String metro, String city, int limit);

	/**
	 * 좌표에서 반경 안에 있을 수 있는 충전소. 경계에 걸친 충전소를 놓치지 않게 조금 넉넉히 가져오므로
	 * 정확한 거리 판단은 호출하는 쪽에서 한다.
	 */
	List<Station> findAllWithin(double latitude, double longitude, double radiusMeters);

}
