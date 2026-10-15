package com.picka_chew.dangcharge_Consumer.api.station.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge_Consumer.api.common.dto.EventMessage;
import com.picka_chew.dangcharge_Consumer.api.station.dto.StationEventPayload;
import com.picka_chew.dangcharge_Consumer.api.station.service.ApplyStationEventUseCase;
import com.picka_chew.dangcharge_Consumer.common.consts.EventTopicStaticValues;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class StationEventListener {

	private static final TypeReference<EventMessage<StationEventPayload>> MESSAGE_TYPE = new TypeReference<>() {
	};

	private final ApplyStationEventUseCase applyStationEventUseCase;
	private final JsonMapper jsonMapper;

	@KafkaListener(id = "station-event-listener", topics = EventTopicStaticValues.STATION)
	public void onMessage(String message) {
		applyStationEventUseCase.execute(jsonMapper.readValue(message, MESSAGE_TYPE));
	}

}
