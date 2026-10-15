package com.picka_chew.dangcharge_Consumer.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 코드와 메시지는 producer와 같게 둔다. 프론트가 두 서버의 오류를 같은 방식으로 처리할 수 있게 하기 위해서다 */
@Getter
@RequiredArgsConstructor
public enum ErrorStatus {

	// 공통
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 에러, 관리자에게 문의 바랍니다"),

	// 사용자
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 사용자입니다"),

	// 충전소
	STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "존재하지 않는 충전소입니다"),
	INVALID_SEARCH_CONDITION(HttpStatus.BAD_REQUEST, "INVALID_SEARCH_CONDITION", "검색어, 시도, 시군구 중 하나 이상을 입력해야 합니다"),
	INVALID_RADIUS(HttpStatus.BAD_REQUEST, "INVALID_RADIUS", "반경은 0보다 크고 20km 이하여야 합니다"),
	INVALID_AVAILABILITY_DATE(HttpStatus.BAD_REQUEST, "INVALID_AVAILABILITY_DATE", "오늘부터 예약 가능 기간 안의 날짜만 조회할 수 있습니다"),

	// 예약·충전
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "존재하지 않는 예약입니다"),
	CHARGING_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CHARGING_HISTORY_NOT_FOUND", "존재하지 않는 충전 내역입니다");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
