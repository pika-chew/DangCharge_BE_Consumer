package com.picka_chew.dangcharge_Consumer.api.reservation.dto;

import java.time.Instant;

import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

/** producer ReservationEvent 본문. 이벤트 종류와 관계없이 예약 전체 스냅샷이다 */
public record ReservationEventPayload(Long reservationId, Long userId, Long chargerId, Long stationId,
		String stationName, String stationAddress, VehicleModel vehicleModel, Instant startsAt, Instant endsAt,
		ReservationStatus status, Instant chargingStartedAt, Instant completedAt) {

	/** @param createdAt 예약 생성 시각. 생성 이벤트가 아니면 null이다 */
	public Reservation toReservation(long eventVersion, Instant createdAt) {
		return Reservation.builder()
				.id(reservationId)
				.userId(userId)
				.chargerId(chargerId)
				.stationId(stationId)
				.stationName(stationName)
				.stationAddress(stationAddress)
				.vehicleModel(vehicleModel)
				.startsAt(startsAt)
				.endsAt(endsAt)
				.status(status)
				.chargingStartedAt(chargingStartedAt)
				.completedAt(completedAt)
				.createdAt(createdAt)
				.eventVersion(eventVersion)
				.build();
	}

}
