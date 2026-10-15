package com.picka_chew.dangcharge_Consumer.domain.station.entity;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 충전소 문서에 들어 있는 충전기. 충전기는 충전소 이벤트에 함께 실려 오고 따로 조회할 일이 없어 내장 문서로 둔다 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "chargerId")
public class Charger {

	/** producer의 충전기 ID. 예약이 가리키는 값이다 */
	private Long chargerId;

	private ChargingType chargingType;

	/** 최대 출력(kW). 없으면 null, 있으면 0보다 크다 */
	private BigDecimal maxPowerKw;

	@Builder.Default
	private OperationalStatus operationalStatus = OperationalStatus.UNKNOWN;

	private ChargerDataSource dataSource;

	/** 고장·점검 중이 아니면 예약할 수 있다. 상태를 모르는 충전기(UNKNOWN)도 예약은 받는다 */
	public boolean isUsable() {
		return operationalStatus != OperationalStatus.OUT_OF_SERVICE;
	}

}
