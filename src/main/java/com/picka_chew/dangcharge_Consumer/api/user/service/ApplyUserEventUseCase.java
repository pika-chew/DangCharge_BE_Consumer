package com.picka_chew.dangcharge_Consumer.api.user.service;

import java.util.Set;

import com.picka_chew.dangcharge_Consumer.api.common.dto.EventMessage;
import com.picka_chew.dangcharge_Consumer.api.user.dto.UserEventPayload;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.user.service.UserDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 사용자 생성·수정 이벤트를 사용자 문서에 반영한다. 두 이벤트 모두 전체 스냅샷이라 같은 방식으로 덮어쓴다 */
@Slf4j
@UseCase
@RequiredArgsConstructor
public class ApplyUserEventUseCase {

	static final Set<String> SUPPORTED_EVENT_TYPES = Set.of("UserCreated", "UserUpdated");

	private final UserDomainService userDomainService;

	public void execute(EventMessage<UserEventPayload> message) {
		if (!SUPPORTED_EVENT_TYPES.contains(message.eventType())) {
			log.warn("처리하지 않는 사용자 이벤트라 건너뜀: eventId={}, type={}", message.eventId(), message.eventType());
			return;
		}
		if (!userDomainService.apply(message.payload().toUser(message.version()))) {
			log.debug("이미 더 새 사용자 이벤트를 반영해 건너뜀: userId={}, version={}", message.aggregateId(),
					message.version());
		}
	}

}
