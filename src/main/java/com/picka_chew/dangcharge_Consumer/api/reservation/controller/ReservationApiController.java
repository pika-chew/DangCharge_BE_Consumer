package com.picka_chew.dangcharge_Consumer.api.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge_Consumer.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge_Consumer.api.reservation.dto.MyReservationResponse;
import com.picka_chew.dangcharge_Consumer.api.reservation.dto.ReservationDetailResponse;
import com.picka_chew.dangcharge_Consumer.api.reservation.service.GetMyReservationUseCase;
import com.picka_chew.dangcharge_Consumer.api.reservation.service.GetReservationUseCase;
import com.picka_chew.dangcharge_Consumer.common.annotation.CurrentUser;
import com.picka_chew.dangcharge_Consumer.security.dto.AuthUser;

import lombok.RequiredArgsConstructor;

@Tag(name = "reservation", description = "[예약] 충전구 예약 조회. 모든 API가 로그인을 요구한다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationApiController {

	private final GetMyReservationUseCase getMyReservationUseCase;
	private final GetReservationUseCase getReservationUseCase;

	@Operation(summary = "내 예약 확인하기",
			description = "진행 중인 내 예약을 확인한다. 사용자당 진행 중인 예약은 하나라 ID 없이 조회한다. "
					+ "state는 NONE(예약 없음), RESERVED(예약됨), CHARGING(충전중)이고 message는 화면에 띄울 문구다. "
					+ "예약이 없어도 오류가 아니라 200이며 reservation이 null이다.")
	@GetMapping("/me")
	public ApiResponseDto<MyReservationResponse> getMyReservation(@CurrentUser AuthUser authUser) {
		return ApiResponseDto.onSuccess(getMyReservationUseCase.execute(authUser.userId()));
	}

	@Operation(summary = "예약 상세확인하기",
			description = "예약 번호, 시간, 충전소, 예약 상태를 확인한다. 취소·완료된 예약도 내 예약이면 조회된다.")
	@GetMapping("/{reservationId}")
	public ApiResponseDto<ReservationDetailResponse> getReservation(@CurrentUser AuthUser authUser,
			@PathVariable Long reservationId) {
		return ApiResponseDto.onSuccess(getReservationUseCase.execute(authUser.userId(), reservationId));
	}

}
