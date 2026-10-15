package com.picka_chew.dangcharge_Consumer.domain.station.service;

import com.picka_chew.dangcharge_Consumer.common.annotation.DomainService;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.station.repository.StationRepository;

import lombok.RequiredArgsConstructor;

@DomainService
@RequiredArgsConstructor
public class StationDomainServiceImpl implements StationDomainService {

	private final StationRepository stationRepository;

	@Override
	public boolean apply(Station station) {
		return stationRepository.replaceIfNewer(station);
	}

}
