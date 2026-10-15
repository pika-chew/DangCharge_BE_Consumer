package com.picka_chew.dangcharge_Consumer.domain.reservation.adaptor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.picka_chew.dangcharge_Consumer.common.annotation.Adaptor;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.common.exception.GeneralException;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge_Consumer.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Adaptor
@RequiredArgsConstructor
public class ReservationAdaptorImpl implements ReservationAdaptor {

	private final ReservationRepository reservationRepository;

	@Override
	public Reservation getByIdAndUserId(Long reservationId, Long userId) {
		return reservationRepository.findByIdAndUserId(reservationId, userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.RESERVATION_NOT_FOUND));
	}

	@Override
	public Reservation getCompletedByIdAndUserId(Long reservationId, Long userId) {
		return reservationRepository.findByIdAndUserId(reservationId, userId)
				.filter(reservation -> reservation.getStatus() == ReservationStatus.COMPLETED)
				.orElseThrow(() -> new GeneralException(ErrorStatus.CHARGING_HISTORY_NOT_FOUND));
	}

	@Override
	public Optional<Reservation> findActiveByUserId(Long userId, Instant now) {
		return reservationRepository.findInProgressByUserId(userId, now).stream().findFirst();
	}

	@Override
	public List<Reservation> findConfirmedOverlapping(Collection<Long> chargerIds, Instant from, Instant to) {
		if (chargerIds.isEmpty()) {
			return List.of();
		}
		return reservationRepository.findAllByChargerIdInAndStatusInAndStartsAtLessThanAndEndsAtGreaterThan(
				chargerIds, ReservationStatus.IN_PROGRESS, to, from);
	}

	@Override
	public Page<Reservation> findCompletedByUserId(Long userId, Pageable pageable) {
		return reservationRepository.findPageByUserIdAndStatus(userId, ReservationStatus.COMPLETED, pageable);
	}

}
