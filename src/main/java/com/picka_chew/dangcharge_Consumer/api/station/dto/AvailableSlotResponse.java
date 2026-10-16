package com.picka_chew.dangcharge_Consumer.api.station.dto;

import java.time.OffsetDateTime;

import com.picka_chew.dangcharge_Consumer.common.util.TimeUtils;
import com.picka_chew.dangcharge_Consumer.domain.reservation.model.AvailableSlot;
import com.picka_chew.dangcharge_Consumer.domain.reservation.model.SlotStatus;

/** @param status ENABLE(예약 가능), DISABLE(이미 예약이 걸려 있음) */
public record AvailableSlotResponse(Long chargerId, OffsetDateTime startAt, OffsetDateTime endAt,
		SlotStatus status) {

	public static AvailableSlotResponse from(AvailableSlot slot) {
		return new AvailableSlotResponse(slot.chargerId(), TimeUtils.toOffsetDateTime(slot.startsAt()),
				TimeUtils.toOffsetDateTime(slot.endsAt()), slot.status());
	}

}
