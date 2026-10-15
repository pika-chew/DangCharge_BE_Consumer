package com.picka_chew.dangcharge_Consumer.domain.reservation.adaptor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;

public interface ReservationAdaptor {

	/** 내 예약이면 상태와 관계없이. 없거나 남의 예약이면 RESERVATION_NOT_FOUND */
	Reservation getByIdAndUserId(Long reservationId, Long userId);

	/** 충전을 마친 내 예약. 없거나 완료 전이면 CHARGING_HISTORY_NOT_FOUND */
	Reservation getCompletedByIdAndUserId(Long reservationId, Long userId);

	/** 진행 중인 예약(예약·충전중). 사용자당 하나지만 여러 개면 시작이 가장 늦은 것 */
	Optional<Reservation> findActiveByUserId(Long userId, Instant now);

	/** 충전기들에서 [from, to)와 겹치는 진행 중 예약 */
	List<Reservation> findConfirmedOverlapping(Collection<Long> chargerIds, Instant from, Instant to);

	/** 충전을 마친 예약을 최근 완료 순으로 */
	Page<Reservation> findCompletedByUserId(Long userId, Pageable pageable);

}
