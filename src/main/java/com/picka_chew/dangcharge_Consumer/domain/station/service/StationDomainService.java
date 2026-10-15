package com.picka_chew.dangcharge_Consumer.domain.station.service;

import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;

public interface StationDomainService {

	/** @return 반영했으면 true, 이미 더 새 이벤트를 반영해 건너뛰었으면 false */
	boolean apply(Station station);

}
