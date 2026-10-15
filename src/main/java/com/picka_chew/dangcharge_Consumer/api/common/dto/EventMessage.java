package com.picka_chew.dangcharge_Consumer.api.common.dto;

import java.time.Instant;

/**
 * producer가 Kafka로 보내는 메시지. producer OutboxRelay가 만드는 형식과 같다.
 *
 * @param version 같은 애그리거트 안에서 이벤트 순서. producer outbox id라 단조 증가한다
 * @param occurredAt producer에서 이벤트를 기록한 시각(UTC)
 * @param payload 이벤트 본문. 화면에 필요한 전체 스냅샷이다
 */
public record EventMessage<T>(Long eventId, String eventType, String aggregateType, Long aggregateId, long version,
		Instant occurredAt, T payload) {
}
