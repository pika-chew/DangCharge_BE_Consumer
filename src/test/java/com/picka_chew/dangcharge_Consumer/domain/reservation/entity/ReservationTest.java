package com.picka_chew.dangcharge_Consumer.domain.reservation.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ReservationTest {

	@Test
	void reservationNumberUsesKoreanCreationDateAndPaddedId() {
		// 2026-10-14 23:30 UTC는 한국 시간으로 10월 15일이다
		Reservation reservation = Reservation.builder().id(42L).createdAt(Instant.parse("2026-10-14T23:30:00Z")).build();

		assertThat(reservation.getReservationNumber()).isEqualTo("DC20261015-000042");
	}

	@Test
	void reservationNumberFallsBackToStartWhenCreationIsUnknown() {
		Reservation reservation = Reservation.builder().id(7L).startsAt(Instant.parse("2026-10-16T03:00:00Z")).build();

		assertThat(reservation.getReservationNumber()).isEqualTo("DC20261016-000007");
	}

	@Test
	void keepsPreviousCreationTimeOnlyWhenThereIsOne() {
		Instant created = Instant.parse("2026-10-15T01:00:00Z");
		Reservation started = Reservation.builder().id(1L).build();
		Reservation createdEvent = Reservation.builder().id(1L).createdAt(created).build();

		started.inheritCreatedAt(created);
		createdEvent.inheritCreatedAt(null);

		assertThat(started.getCreatedAt()).isEqualTo(created);
		assertThat(createdEvent.getCreatedAt()).isEqualTo(created);
	}

}
