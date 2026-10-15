package com.picka_chew.dangcharge_Consumer.api.station.service;

import java.util.Set;

import com.picka_chew.dangcharge_Consumer.api.common.dto.EventMessage;
import com.picka_chew.dangcharge_Consumer.api.station.dto.StationEventPayload;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.station.service.StationDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 충전소 동기화 이벤트를 충전소 문서에 반영한다. 소속 충전기 목록도 이벤트 값으로 통째로 바꾼다 */
@Slf4j
@UseCase
@RequiredArgsConstructor
public class ApplyStationEventUseCase {

	static final Set<String> SUPPORTED_EVENT_TYPES = Set.of("StationUpserted");

	private final StationDomainService stationDomainService;

	public void execute(EventMessage<StationEventPayload> message) {
		if (!SUPPORTED_EVENT_TYPES.contains(message.eventType())) {
			log.warn("처리하지 않는 충전소 이벤트라 건너뜀: eventId={}, type={}", message.eventId(), message.eventType());
			return;
		}
		if (!stationDomainService.apply(message.payload().toStation(message.version()))) {
			log.debug("이미 더 새 충전소 이벤트를 반영해 건너뜀: stationId={}, version={}", message.aggregateId(),
					message.version());
		}
	}

}
