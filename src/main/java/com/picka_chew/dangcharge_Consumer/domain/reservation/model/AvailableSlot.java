package com.picka_chew.dangcharge_Consumer.domain.reservation.model;

import java.time.Instant;

/** 충전기 하나의 예약 슬롯. 이미 예약이 걸린 슬롯은 status가 DISABLE이다 */
public record AvailableSlot(Long chargerId, Instant startsAt, Instant endsAt, SlotStatus status) {
}
