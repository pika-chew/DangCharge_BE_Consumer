package com.picka_chew.dangcharge_Consumer.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge_Consumer.domain.reservation.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationDomainServiceImplTest {

	@Mock
	ReservationRepository reservationRepository;
	@InjectMocks
	ReservationDomainServiceImpl domainService;

	@Test
	void laterEventKeepsCreationTimeFromCreatedEvent() {
		Instant created = Instant.parse("2026-10-15T01:00:00Z");
		given(reservationRepository.findById(1L)).willReturn(Optional.of(
				Reservation.builder().id(1L).status(ReservationStatus.CONFIRMED).createdAt(created).eventVersion(3).build()));
		Reservation charging = Reservation.builder().id(1L).status(ReservationStatus.CHARGING).eventVersion(5).build();
		given(reservationRepository.replaceIfNewer(charging)).willReturn(true);

		assertThat(domainService.apply(charging)).isTrue();

		verify(reservationRepository).replaceIfNewer(charging);
		assertThat(charging.getCreatedAt()).isEqualTo(created);
	}

}
