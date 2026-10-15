package com.picka_chew.dangcharge_Consumer.domain.reservation.entity;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.picka_chew.dangcharge_Consumer.common.util.TimeUtils;
import com.picka_chew.dangcharge_Consumer.domain.common.entity.EventVersionedDocument;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예약 조회용 문서. producer의 예약 이벤트(생성·취소·충전 시작·충전 완료)로 만든다.
 * 충전소 이름·주소를 함께 담아 조인 없이 화면을 그린다.
 */
@Document("reservations")
@CompoundIndex(name = "idx_reservations_user_status_ends", def = "{'userId': 1, 'status': 1, 'endsAt': 1}")
@CompoundIndex(name = "idx_reservations_user_status_completed",
		def = "{'userId': 1, 'status': 1, 'completedAt': -1, '_id': -1}")
@CompoundIndex(name = "idx_reservations_charger_status_time",
		def = "{'chargerId': 1, 'status': 1, 'startsAt': 1, 'endsAt': 1}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class Reservation implements EventVersionedDocument {

	private static final DateTimeFormatter RESERVATION_NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

	/** producer의 예약 ID */
	@Id
	private Long id;

	private Long userId;

	private Long chargerId;

	private Long stationId;

	private String stationName;

	private String stationAddress;

	/** 예약 당시 사용자의 차종 */
	private VehicleModel vehicleModel;

	/** 예약 시작(UTC) */
	private Instant startsAt;

	/** 예약 종료(UTC) */
	private Instant endsAt;

	private ReservationStatus status;

	/** 충전 시작(CHARGING) 시각(UTC) */
	private Instant chargingStartedAt;

	/** 충전 완료(COMPLETED) 시각(UTC) */
	private Instant completedAt;

	/**
	 * 예약 생성 시각(UTC). producer 이벤트 본문에는 없어서 ReservationCreated 이벤트의 발생 시각을 쓴다.
	 * 예약 생성과 이벤트 기록이 같은 트랜잭션이라 producer의 생성 시각과 같은 날이다.
	 */
	private Instant createdAt;

	private long eventVersion;

	/** 사용자에게 보여주는 예약 번호. producer와 같은 규칙이다. 예: DC20261015-000001 */
	public String getReservationNumber() {
		Instant created = createdAt == null ? startsAt : createdAt;
		return "DC" + RESERVATION_NUMBER_DATE.format(created.atZone(TimeUtils.ZONE)) + "-" + String.format("%06d", id);
	}

	/** 생성 시각은 생성 이벤트에만 있어서, 뒤따르는 이벤트로 문서를 바꿀 때 앞서 저장한 값을 이어받는다 */
	public void inheritCreatedAt(Instant previousCreatedAt) {
		if (previousCreatedAt != null) {
			this.createdAt = previousCreatedAt;
		}
	}

}
