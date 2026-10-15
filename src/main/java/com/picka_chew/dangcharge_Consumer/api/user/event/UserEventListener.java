package com.picka_chew.dangcharge_Consumer.api.user.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge_Consumer.api.common.dto.EventMessage;
import com.picka_chew.dangcharge_Consumer.api.user.dto.UserEventPayload;
import com.picka_chew.dangcharge_Consumer.api.user.service.ApplyUserEventUseCase;
import com.picka_chew.dangcharge_Consumer.common.consts.EventTopicStaticValues;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class UserEventListener {

	private static final TypeReference<EventMessage<UserEventPayload>> MESSAGE_TYPE = new TypeReference<>() {
	};

	private final ApplyUserEventUseCase applyUserEventUseCase;
	private final JsonMapper jsonMapper;

	@KafkaListener(id = "user-event-listener", idIsGroup = false, topics = EventTopicStaticValues.USER)
	public void onMessage(String message) {
		applyUserEventUseCase.execute(jsonMapper.readValue(message, MESSAGE_TYPE));
	}

}
