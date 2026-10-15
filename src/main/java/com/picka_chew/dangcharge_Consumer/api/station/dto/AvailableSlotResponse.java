package com.picka_chew.dangcharge_Consumer.api.station.dto;

import java.time.OffsetDateTime;

import com.picka_chew.dangcharge_Consumer.common.util.TimeUtils;
import com.picka_chew.dangcharge_Consumer.domain.reservation.model.AvailableSlot;

public record AvailableSlotResponse(Long chargerId, OffsetDateTime startAt, OffsetDateTime endAt) {

	public static AvailableSlotResponse from(AvailableSlot slot) {
		return new AvailableSlotResponse(slot.chargerId(), TimeUtils.toOffsetDateTime(slot.startsAt()),
				TimeUtils.toOffsetDateTime(slot.endsAt()));
	}

}
