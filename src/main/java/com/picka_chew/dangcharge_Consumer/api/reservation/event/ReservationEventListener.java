package com.picka_chew.dangcharge_Consumer.api.reservation.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge_Consumer.api.common.dto.EventMessage;
import com.picka_chew.dangcharge_Consumer.api.reservation.dto.ReservationEventPayload;
import com.picka_chew.dangcharge_Consumer.api.reservation.service.ApplyReservationEventUseCase;
import com.picka_chew.dangcharge_Consumer.common.consts.EventTopicStaticValues;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {

	private static final TypeReference<EventMessage<ReservationEventPayload>> MESSAGE_TYPE = new TypeReference<>() {
	};

	private final ApplyReservationEventUseCase applyReservationEventUseCase;
	private final JsonMapper jsonMapper;

	@KafkaListener(id = "reservation-event-listener", topics = EventTopicStaticValues.RESERVATION)
	public void onMessage(String message) {
		applyReservationEventUseCase.execute(jsonMapper.readValue(message, MESSAGE_TYPE));
	}

}
