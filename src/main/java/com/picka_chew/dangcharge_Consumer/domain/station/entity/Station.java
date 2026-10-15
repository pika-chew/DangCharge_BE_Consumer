package com.picka_chew.dangcharge_Consumer.domain.station.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.picka_chew.dangcharge_Consumer.domain.common.entity.EventVersionedDocument;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 충전소 조회용 문서. producer의 StationUpserted 이벤트로 만들고, 소속 충전기를 함께 담는다.
 */
@Document("stations")
@CompoundIndex(name = "idx_stations_metro_city", def = "{'metro': 1, 'city': 1}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class Station implements EventVersionedDocument {

	/** producer의 충전소 ID */
	@Id
	private Long id;

	/** 충전소 이름 */
	private String name;

	private String address;

	/** 시도 */
	private String metro;

	/** 시군구 */
	private String city;

	/** 급속 충전기 수 */
	private int rapidCount;

	/** 완속 충전기 수 */
	private int slowCount;

	/** 이 충전소에서 충전할 수 있는 차종 */
	@Builder.Default
	private Set<VehicleModel> supportedModels = new HashSet<>();

	/** 위도. 없으면 null, 있으면 -90~90 */
	private BigDecimal latitude;

	/** 경도. 없으면 null, 있으면 -180~180 */
	private BigDecimal longitude;

	/** 반경 검색용 좌표. 위경도가 없으면 null이고, 그런 충전소는 반경 검색에 나오지 않는다 */
	@GeoSpatialIndexed(name = "idx_stations_location", type = GeoSpatialIndexType.GEO_2DSPHERE)
	private GeoJsonPoint location;

	/** producer에서 마지막으로 동기화한 시각(UTC) */
	private Instant syncedAt;

	/** 소속 충전기. 충전기 ID 순이다 */
	@Builder.Default
	private List<Charger> chargers = new ArrayList<>();

	private long eventVersion;

	/** GeoJSON은 경도, 위도 순서다 */
	public static GeoJsonPoint toLocation(BigDecimal latitude, BigDecimal longitude) {
		if (latitude == null || longitude == null) {
			return null;
		}
		return new GeoJsonPoint(longitude.doubleValue(), latitude.doubleValue());
	}

	/** 주어진 차종을 이 충전소에서 충전할 수 있는지 */
	public boolean supports(VehicleModel model) {
		return supportedModels.contains(model);
	}

}
