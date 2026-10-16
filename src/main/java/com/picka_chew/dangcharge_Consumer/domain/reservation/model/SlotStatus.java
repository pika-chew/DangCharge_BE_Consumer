package com.picka_chew.dangcharge_Consumer.domain.reservation.model;

/** 예약 슬롯을 지금 예약할 수 있는지 */
public enum SlotStatus {

	/** 겹치는 예약이 없어 예약할 수 있다 */
	ENABLE,
	/** 이미 예약(예약·충전중)이 걸려 있어 예약할 수 없다 */
	DISABLE

}
