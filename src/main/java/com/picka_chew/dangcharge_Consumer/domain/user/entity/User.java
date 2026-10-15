package com.picka_chew.dangcharge_Consumer.domain.user.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.picka_chew.dangcharge_Consumer.domain.common.entity.EventVersionedDocument;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 조회용 문서. producer의 UserCreated·UserUpdated 이벤트로 만든다. 비밀번호 해시는 받지 않는다 */
@Document("users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class User implements EventVersionedDocument {

	/** producer의 사용자 ID */
	@Id
	private Long id;

	/** 로그인 아이디 */
	private String loginId;

	/** 유저네임 */
	private String username;

	/** 차종. 충전소의 지원 차종과 비교하는 기준이다 */
	private VehicleModel vehicleModel;

	/** 가입 시각(UTC) */
	private Instant createdAt;

	private long eventVersion;

}
