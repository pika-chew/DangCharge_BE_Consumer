package com.picka_chew.dangcharge_Consumer.domain.station.policy;

/**
 * 거리 계산 규칙. producer와 같은 지구 반지름과 하버사인 공식을 써서 두 서버의 거리가 같게 나오게 한다.
 */
public final class GeoDistancePolicy {

	public static final double EARTH_RADIUS_METERS = 6_371_000;

	private GeoDistancePolicy() {
	}

	/** 두 좌표 사이 거리(m) */
	public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		return 2 * EARTH_RADIUS_METERS * Math.asin(Math.min(1, Math.sqrt(a)));
	}

	/** 거리(m)를 구면 위 중심각(라디안)으로 바꾼다. MongoDB $centerSphere 반경 단위다 */
	public static double toRadians(double meters) {
		return meters / EARTH_RADIUS_METERS;
	}

}
