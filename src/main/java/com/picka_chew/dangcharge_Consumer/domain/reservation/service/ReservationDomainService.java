package com.picka_chew.dangcharge_Consumer.domain.reservation.service;

import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;

public interface ReservationDomainService {

	/**
	 * 이벤트 스냅샷으로 예약 문서를 바꾼다. 앞서 저장한 생성 시각은 유지한다.
	 *
	 * @return 반영했으면 true, 이미 더 새 이벤트를 반영해 건너뛰었으면 false
	 */
	boolean apply(Reservation reservation);

}
