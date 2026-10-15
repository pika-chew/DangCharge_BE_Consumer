package com.picka_chew.dangcharge_Consumer.domain.user.adaptor;

import com.picka_chew.dangcharge_Consumer.common.annotation.Adaptor;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.common.exception.GeneralException;
import com.picka_chew.dangcharge_Consumer.domain.user.entity.User;
import com.picka_chew.dangcharge_Consumer.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Adaptor
@RequiredArgsConstructor
public class UserAdaptorImpl implements UserAdaptor {

	private final UserRepository userRepository;

	@Override
	public User getById(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
	}

}
