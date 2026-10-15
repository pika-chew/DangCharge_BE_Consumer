package com.picka_chew.dangcharge_Consumer.domain.station.repository;

import java.util.List;

import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;

/** 조건이 선택적인 검색이라 메서드 이름 쿼리 대신 직접 조건을 만든다 */
public interface StationSearchRepository {

	/**
	 * 이름·주소에 검색어가 들어 있고 시도·시군구가 같은 충전소를 이름, ID 순으로. null인 조건은 보지 않는다.
	 */
	List<Station> search(String keyword, String metro, String city, int limit);

}
