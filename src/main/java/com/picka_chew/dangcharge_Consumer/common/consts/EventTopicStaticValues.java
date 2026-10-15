package com.picka_chew.dangcharge_Consumer.common.consts;

/** producer가 도메인 이벤트를 보내는 토픽. producer OutboxRelay의 dangcharge.{aggregateType}와 같다 */
public final class EventTopicStaticValues {

	public static final String USER = "dangcharge.user";
	public static final String STATION = "dangcharge.station";
	public static final String RESERVATION = "dangcharge.reservation";

	/** 끝내 처리하지 못한 메시지를 옮기는 토픽 접미사. 예: dangcharge.station.DLT */
	public static final String DEAD_LETTER_SUFFIX = ".DLT";

	private EventTopicStaticValues() {
	}

}
