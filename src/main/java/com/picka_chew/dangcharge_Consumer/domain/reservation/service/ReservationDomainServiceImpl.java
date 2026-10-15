package com.picka_chew.dangcharge_Consumer.domain.reservation.service;

import com.picka_chew.dangcharge_Consumer.common.annotation.DomainService;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@DomainService
@RequiredArgsConstructor
public class ReservationDomainServiceImpl implements ReservationDomainService {

	private final ReservationRepository reservationRepository;

	@Override
	public boolean apply(Reservation reservation) {
		reservationRepository.findById(reservation.getId())
				.ifPresent(previous -> reservation.inheritCreatedAt(previous.getCreatedAt()));
		return reservationRepository.replaceIfNewer(reservation);
	}

}
