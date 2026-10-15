package com.picka_chew.dangcharge_Consumer.domain.user.service;

import com.picka_chew.dangcharge_Consumer.common.annotation.DomainService;
import com.picka_chew.dangcharge_Consumer.domain.user.entity.User;
import com.picka_chew.dangcharge_Consumer.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@DomainService
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {

	private final UserRepository userRepository;

	@Override
	public boolean apply(User user) {
		return userRepository.replaceIfNewer(user);
	}

}
