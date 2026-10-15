package com.picka_chew.dangcharge_Consumer.domain.reservation.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.picka_chew.dangcharge_Consumer.domain.common.repository.EventVersionedRepository;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.ReservationStatus;

public interface ReservationRepository extends MongoRepository<Reservation, Long>,
		EventVersionedRepository<Reservation> {

	Optional<Reservation> findByIdAndUserId(Long id, Long userId);

	/** 진행 중인 예약: 아직 끝나지 않은 CONFIRMED, 또는 종료 시각과 관계없이 CHARGING */
	@Query(value = """
			{ 'userId': ?0, '$or': [
				{ 'status': 'CHARGING' },
				{ 'status': 'CONFIRMED', 'endsAt': { '$gt': ?1 } } ] }
			""", sort = "{ 'startsAt': -1 }")
	List<Reservation> findInProgressByUserId(Long userId, Instant now);

	/** 시간이 겹치는 예약. 조건은 기존.시작 < 구간.종료 AND 기존.종료 > 구간.시작 */
	List<Reservation> findAllByChargerIdInAndStatusInAndStartsAtLessThanAndEndsAtGreaterThan(
			Collection<Long> chargerIds, Collection<ReservationStatus> statuses, Instant endsAt, Instant startsAt);

	@Query(value = "{ 'userId': ?0, 'status': ?1 }", sort = "{ 'completedAt': -1, '_id': -1 }")
	Page<Reservation> findPageByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

}
