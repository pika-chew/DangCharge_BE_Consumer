package com.picka_chew.dangcharge_Consumer.api.user.dto;

import java.time.Instant;

import com.picka_chew.dangcharge_Consumer.domain.user.entity.User;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

/** producer UserEvent 본문 */
public record UserEventPayload(Long userId, String loginId, String username, VehicleModel vehicleModel,
		Instant createdAt) {

	public User toUser(long eventVersion) {
		return User.builder()
				.id(userId)
				.loginId(loginId)
				.username(username)
				.vehicleModel(vehicleModel)
				.createdAt(createdAt)
				.eventVersion(eventVersion)
				.build();
	}

}
