package com.picka_chew.dangcharge_Consumer.domain.vehicle.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자가 고를 수 있는 차종. producer의 VehicleModel과 같은 목록이다.
 */
@Getter
@RequiredArgsConstructor
public enum VehicleModel {

	SM3_ZE("SM3 Z.E"),
	RAY_EV("레이EV"),
	SOUL_EV("소울EV"),
	NISSAN_LEAF("닛산리프"),
	IONIQ_EV("아이오닉EV"),
	BMW_I3("BMW i3"),
	SPARK_EV("스파크EV"),
	BOLT_EV("볼트EV"),
	TESLA("테슬라");

	/** 한전 설치현황에 적힌 차종 이름. 화면에 보여줄 이름으로도 쓴다 */
	private final String label;

}
