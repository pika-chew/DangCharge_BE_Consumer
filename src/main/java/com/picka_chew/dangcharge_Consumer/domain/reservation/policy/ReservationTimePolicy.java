package com.picka_chew.dangcharge_Consumer.domain.reservation.policy;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.picka_chew.dangcharge_Consumer.common.util.TimeUtils;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.model.AvailableSlot;
import com.picka_chew.dangcharge_Consumer.domain.reservation.model.SlotStatus;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Charger;

/**
 * 예약 시간 규칙 중 조회에 필요한 부분. producer의 ReservationTimePolicy와 값이 같아야 한다.
 * 충전소 운영시간 데이터가 없어 하루 24시간을 예약 가능 시간으로 본다. 날짜·슬롯 경계는 한국 시간 기준이다.
 */
public final class ReservationTimePolicy {

	public static final ZoneId ZONE = TimeUtils.ZONE;
	/** 예약 시작 시각 단위. 정각 또는 30분에 시작한다 */
	public static final Duration SLOT = Duration.ofMinutes(30);
	/** 예약 한 건의 이용 시간 */
	public static final Duration DURATION = Duration.ofHours(1);
	/** 오늘부터 며칠 뒤까지 예약할 수 있는지 */
	public static final int MAX_DAYS_AHEAD = 7;

	private ReservationTimePolicy() {
	}

	public static Instant endOf(Instant startsAt) {
		return startsAt.plus(DURATION);
	}

	/** now 기준으로 예약할 수 있는 날짜인지. 오늘부터 MAX_DAYS_AHEAD일 뒤까지 */
	public static boolean isBookableDate(LocalDate date, Instant now) {
		LocalDate today = LocalDate.ofInstant(now, ZONE);
		return !date.isBefore(today) && !date.isAfter(today.plusDays(MAX_DAYS_AHEAD));
	}

	public static Instant startOfDay(LocalDate date) {
		return date.atStartOfDay(ZONE).toInstant();
	}

	/**
	 * 그날 충전기별 예약 슬롯(30분 간격 시작, 1시간 이용). 겹치는 예약이 있으면 DISABLE, 없으면 ENABLE이다.
	 * 이미 지난 시작 시각과 쓸 수 없는 충전기는 뺀다. 23:30 시작은 다음 날 00:30에 끝난다.
	 *
	 * @param reservations 그날 00:00부터 다음 날 01:00까지와 겹치는 진행 중 예약
	 */
	public static List<AvailableSlot> slots(Collection<Charger> chargers,
			Collection<Reservation> reservations, LocalDate date, Instant now) {
		Instant dayStart = startOfDay(date);
		Instant dayEnd = startOfDay(date.plusDays(1));
		List<AvailableSlot> slots = new ArrayList<>();
		for (Charger charger : chargers) {
			if (!charger.isUsable()) {
				continue;
			}
			for (Instant start = dayStart; start.isBefore(dayEnd); start = start.plus(SLOT)) {
				Instant end = endOf(start);
				if (start.isBefore(now)) {
					continue;
				}
				SlotStatus status = overlapsAny(charger.getChargerId(), start, end, reservations)
						? SlotStatus.DISABLE
						: SlotStatus.ENABLE;
				slots.add(new AvailableSlot(charger.getChargerId(), start, end, status));
			}
		}
		return slots;
	}

	private static boolean overlapsAny(Long chargerId, Instant start, Instant end,
			Collection<Reservation> reservations) {
		for (Reservation reservation : reservations) {
			if (reservation.getChargerId().equals(chargerId)
					&& reservation.getStartsAt().isBefore(end) && reservation.getEndsAt().isAfter(start)) {
				return true;
			}
		}
		return false;
	}

}
