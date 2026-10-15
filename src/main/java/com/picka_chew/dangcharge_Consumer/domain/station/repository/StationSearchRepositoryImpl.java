package com.picka_chew.dangcharge_Consumer.domain.station.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StationSearchRepositoryImpl implements StationSearchRepository {

	private final MongoOperations mongoOperations;

	@Override
	public List<Station> search(String keyword, String metro, String city, int limit) {
		List<Criteria> conditions = new ArrayList<>();
		if (keyword != null) {
			// 검색어의 정규식 특수문자는 글자 그대로 찾는다. producer(MySQL LIKE)처럼 대소문자를 가리지 않는다
			Pattern contains = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
			conditions.add(new Criteria().orOperator(Criteria.where("name").regex(contains),
					Criteria.where("address").regex(contains)));
		}
		if (metro != null) {
			conditions.add(Criteria.where("metro").is(metro));
		}
		if (city != null) {
			conditions.add(Criteria.where("city").is(city));
		}
		Query query = new Query(conditions.isEmpty() ? new Criteria() : new Criteria().andOperator(conditions))
				.with(Sort.by("name", "_id"))
				.limit(limit);
		return mongoOperations.find(query, Station.class);
	}

}
