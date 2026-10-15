package com.picka_chew.dangcharge_Consumer.domain.station.repository;

import java.util.List;

import org.springframework.data.mongodb.core.geo.Sphere;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.picka_chew.dangcharge_Consumer.domain.common.repository.EventVersionedRepository;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;

public interface StationRepository extends MongoRepository<Station, Long>, EventVersionedRepository<Station>,
		StationSearchRepository {

	/** 구면 원 안에 좌표가 있는 충전소($geoWithin $centerSphere). 좌표가 없는 충전소는 나오지 않는다 */
	List<Station> findAllByLocationWithin(Sphere sphere);

}
