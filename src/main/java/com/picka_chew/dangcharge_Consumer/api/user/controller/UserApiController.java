package com.picka_chew.dangcharge_Consumer.api.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge_Consumer.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge_Consumer.api.user.dto.UserResponse;
import com.picka_chew.dangcharge_Consumer.api.user.service.GetMyInfoUseCase;
import com.picka_chew.dangcharge_Consumer.common.annotation.CurrentUser;
import com.picka_chew.dangcharge_Consumer.security.dto.AuthUser;

import lombok.RequiredArgsConstructor;

@Tag(name = "user", description = "[마이페이지] 내 정보")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserApiController {

	private final GetMyInfoUseCase getMyInfoUseCase;

	@Operation(summary = "내 정보 조회", description = "로그인이 필요하다.")
	@GetMapping("/me")
	public ApiResponseDto<UserResponse> getMe(@CurrentUser AuthUser authUser) {
		return ApiResponseDto.onSuccess(getMyInfoUseCase.execute(authUser.userId()));
	}

}
