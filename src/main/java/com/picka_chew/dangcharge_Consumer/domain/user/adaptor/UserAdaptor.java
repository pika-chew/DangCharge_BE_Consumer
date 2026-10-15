package com.picka_chew.dangcharge_Consumer.domain.user.adaptor;

import com.picka_chew.dangcharge_Consumer.domain.user.entity.User;

public interface UserAdaptor {

	/** 없으면 USER_NOT_FOUND. producer에서 가입한 직후에는 이벤트가 반영되기 전이라 잠깐 없을 수 있다 */
	User getById(Long userId);

}
