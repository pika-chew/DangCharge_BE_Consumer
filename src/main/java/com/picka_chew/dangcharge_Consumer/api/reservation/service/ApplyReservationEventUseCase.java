package com.picka_chew.dangcharge_Consumer.api.reservation.service;

import java.time.Instant;
import java.util.Set;

import com.picka_chew.dangcharge_Consumer.api.common.dto.EventMessage;
import com.picka_chew.dangcharge_Consumer.api.reservation.dto.ReservationEventPayload;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.reservation.service.ReservationDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예약 생성·취소·충전 시작·충전 완료 이벤트를 예약 문서에 반영한다. 모두 전체 스냅샷이라 같은 방식으로 덮어쓰고,
 * 생성 이벤트일 때만 발생 시각을 예약 생성 시각으로 남긴다.
 */
@Slf4j
@UseCase
@RequiredArgsConstructor
public class ApplyReservationEventUseCase {

	static final String CREATED = "ReservationCreated";
	static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(CREATED, "ReservationCanceled", "ChargingStarted",
			"ChargingCompleted");

	private final ReservationDomainService reservationDomainService;

	public void execute(EventMessage<ReservationEventPayload> message) {
		if (!SUPPORTED_EVENT_TYPES.contains(message.eventType())) {
			log.warn("처리하지 않는 예약 이벤트라 건너뜀: eventId={}, type={}", message.eventId(), message.eventType());
			return;
		}
		Instant createdAt = CREATED.equals(message.eventType()) ? message.occurredAt() : null;
		if (!reservationDomainService.apply(message.payload().toReservation(message.version(), createdAt))) {
			log.debug("이미 더 새 예약 이벤트를 반영해 건너뜀: reservationId={}, version={}", message.aggregateId(),
					message.version());
		}
	}

}
