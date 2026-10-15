package com.picka_chew.dangcharge_Consumer.api.user.service;

import com.picka_chew.dangcharge_Consumer.api.user.dto.UserResponse;
import com.picka_chew.dangcharge_Consumer.common.annotation.UseCase;
import com.picka_chew.dangcharge_Consumer.domain.user.adaptor.UserAdaptor;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetMyInfoUseCase {

	private final UserAdaptor userAdaptor;

	public UserResponse execute(Long userId) {
		return UserResponse.from(userAdaptor.getById(userId));
	}

}
