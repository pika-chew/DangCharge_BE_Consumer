package com.picka_chew.dangcharge_Consumer.domain.user.service;

import com.picka_chew.dangcharge_Consumer.domain.user.entity.User;

public interface UserDomainService {

	/** @return 반영했으면 true, 이미 더 새 이벤트를 반영해 건너뛰었으면 false */
	boolean apply(User user);

}
