package com.picka_chew.dangcharge_Consumer.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;

import com.picka_chew.dangcharge_Consumer.common.consts.EventTopicStaticValues;
import com.picka_chew.dangcharge_Consumer.common.util.TimeUtils;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge_Consumer.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge_Consumer.domain.reservation.repository.ReservationRepository;
import com.picka_chew.dangcharge_Consumer.domain.station.entity.Station;
import com.picka_chew.dangcharge_Consumer.domain.station.repository.StationRepository;
import com.picka_chew.dangcharge_Consumer.domain.user.repository.UserRepository;
import com.picka_chew.dangcharge_Consumer.security.TestJwtIssuer;

/**
 * 실제 Kafka와 MongoDB로 producer 메시지를 받아 조회 API까지 확인한다. Docker가 없으면 건너뛰지 않고 실패한다.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Import(EventProjectionScenarioTest.Topics.class)
class EventProjectionScenarioTest {

	private static final TestJwtIssuer TOKENS = new TestJwtIssuer();
	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	@Container
	@ServiceConnection
	static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:3.9.1");

	@Container
	@ServiceConnection
	static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

	@Autowired
	MockMvc mockMvc;
	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;
	@Autowired
	StationRepository stationRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ReservationRepository reservationRepository;

	@DynamicPropertySource
	static void jwtPublicKey(DynamicPropertyRegistry registry) {
		registry.add("dangcharge.jwt.public-key", TOKENS::publicKeyBase64);
	}

	@Test
	void stationEventsBuildSearchableStationAndIgnoreStaleVersions() throws Exception {
		send(EventTopicStaticValues.STATION, 101L, message(10, "StationUpserted", "station", 101,
				station(101, "강남구청 충전소", "37.5172363", "127.0473248")));
		send(EventTopicStaticValues.STATION, 101L, message(5, "StationUpserted", "station", 101,
				station(101, "오래된 이름", "37.5172363", "127.0473248")));
		send(EventTopicStaticValues.STATION, 102L, message(11, "StationUpserted", "station", 102,
				station(102, "좌표 없는 충전소", null, null)));

		await().atMost(TIMEOUT).until(() -> stationRepository.findById(102L).isPresent());
		Station station = stationRepository.findById(101L).orElseThrow();
		assertThat(station.getName()).isEqualTo("강남구청 충전소");
		assertThat(station.getEventVersion()).isEqualTo(10L);

		mockMvc.perform(get("/api/v1/stations/101"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("강남구청 충전소"))
				.andExpect(jsonPath("$.data.latitude").value(37.5172363))
				.andExpect(jsonPath("$.data.chargers[0].chargerId").value(1011))
				.andExpect(jsonPath("$.data.chargers[0].maxPowerKw").value(50.0))
				.andExpect(jsonPath("$.data.chargers[1].status").value("OUT_OF_SERVICE"));
		mockMvc.perform(get("/api/v1/stations?lat=37.5172&lng=127.0473&radius=500&availableOnly=true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].stationId").value(101))
				.andExpect(jsonPath("$.data[0].availableChargerCount").value(1))
				.andExpect(jsonPath("$.data[*].stationId", not(hasItem(102))));
		mockMvc.perform(get("/api/v1/stations?lat=37.6&lng=127.0473&radius=1000"))
				.andExpect(jsonPath("$.data.length()").value(0));
		mockMvc.perform(get("/api/v1/stations/search?keyword=구청&metro=서울시"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].stationId").value(101))
				.andExpect(jsonPath("$.data[0].supportedModels[0]").value("TESLA"));
	}

	@Test
	void reservationLifecycleShowsUpInReservationAndChargingHistoryApis() throws Exception {
		Instant createdAt = Instant.parse("2026-10-14T23:30:00Z");
		send(EventTopicStaticValues.USER, 7L, message(30, "UserCreated", "user", 7,
				"{\"userId\":7,\"loginId\":\"driver\",\"username\":\"운전자\",\"vehicleModel\":\"TESLA\","
						+ "\"createdAt\":\"2026-10-01T00:00:00Z\"}"));
		send(EventTopicStaticValues.RESERVATION, 900L, message(31, "ReservationCreated", "reservation", 900,
				reservation(900, "CONFIRMED", null, null), createdAt));
		send(EventTopicStaticValues.RESERVATION, 900L, message(32, "ChargingStarted", "reservation", 900,
				reservation(900, "CHARGING", "\"2026-10-15T02:55:00Z\"", null), createdAt.plusSeconds(3600)));

		await().atMost(TIMEOUT).until(() -> reservationRepository.findById(900L)
				.map(Reservation::getStatus).filter(ReservationStatus.CHARGING::equals).isPresent());
		mockMvc.perform(get("/api/v1/reservations/me").header(HttpHeaders.AUTHORIZATION, TOKENS.bearer(7L)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.state").value("CHARGING"))
				.andExpect(jsonPath("$.data.reservation.stationName").value("강남구청 충전소"));

		send(EventTopicStaticValues.RESERVATION, 900L, message(33, "ChargingCompleted", "reservation", 900,
				reservation(900, "COMPLETED", "\"2026-10-15T02:55:00Z\"", "\"2026-10-15T03:50:00Z\""),
				createdAt.plusSeconds(7200)));
		await().atMost(TIMEOUT).until(() -> reservationRepository.findById(900L)
				.map(Reservation::getStatus).filter(ReservationStatus.COMPLETED::equals).isPresent());

		String bearer = TOKENS.bearer(7L);
		mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(jsonPath("$.data.username").value("운전자"));
		mockMvc.perform(get("/api/v1/reservations/900").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				// 생성 이벤트의 시각(한국 시간 10월 15일)을 뒤 이벤트가 덮지 않는다
				.andExpect(jsonPath("$.data.reservationNumber").value("DC20261015-000900"))
				.andExpect(jsonPath("$.data.status").value("COMPLETED"))
				.andExpect(jsonPath("$.data.startAt").value("2026-10-15T12:00:00+09:00"));
		mockMvc.perform(get("/api/v1/reservations/me").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(jsonPath("$.data.state").value("NONE"));
		mockMvc.perform(get("/api/v1/charging/history").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalElements").value(1))
				.andExpect(jsonPath("$.data.content[0].reservationId").value(900))
				.andExpect(jsonPath("$.data.content[0].completedAt").value("2026-10-15T12:50:00+09:00"));
		mockMvc.perform(get("/api/v1/reservations/900").header(HttpHeaders.AUTHORIZATION, TOKENS.bearer(8L)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("RESERVATION_NOT_FOUND"));
	}

	@Test
	void availabilityDisablesSlotsTakenByConfirmedReservations() throws Exception {
		LocalDate tomorrow = LocalDate.now(TimeUtils.ZONE).plusDays(1);
		Instant noon = tomorrow.atTime(12, 0).atZone(TimeUtils.ZONE).toInstant();
		send(EventTopicStaticValues.USER, 70L, message(40, "UserCreated", "user", 70,
				"{\"userId\":70,\"loginId\":\"tesla\",\"username\":\"테슬라\",\"vehicleModel\":\"TESLA\"}"));
		send(EventTopicStaticValues.STATION, 300L, message(41, "StationUpserted", "station", 300,
				station(300, "예약 충전소", "37.4", "127.1")));
		send(EventTopicStaticValues.RESERVATION, 901L, message(42, "ReservationCreated", "reservation", 901,
				"{\"reservationId\":901,\"userId\":71,\"chargerId\":3001,\"stationId\":300,\"stationName\":\"예약 충전소\","
						+ "\"stationAddress\":\"주소\",\"vehicleModel\":\"TESLA\",\"startsAt\":\"" + noon + "\","
						+ "\"endsAt\":\"" + noon.plusSeconds(3600) + "\",\"status\":\"CONFIRMED\"}"));

		await().atMost(TIMEOUT).until(() -> userRepository.findById(70L).isPresent()
				&& stationRepository.findById(300L).isPresent() && reservationRepository.findById(901L).isPresent());
		String day = tomorrow.format(DateTimeFormatter.ISO_DATE);
		mockMvc.perform(get("/api/v1/stations/300/availability?date=" + day)
						.header(HttpHeaders.AUTHORIZATION, TOKENS.bearer(70L)))
				.andExpect(status().isOk())
				// 고장 난 3002번은 빠지고 3001번 48개가 나온다. 12:00 예약과 겹치는 3개만 DISABLE이다
				.andExpect(jsonPath("$.data.length()").value(48))
				.andExpect(jsonPath("$.data[?(@.status == 'DISABLE')].startAt", contains(
						day + "T11:30:00+09:00", day + "T12:00:00+09:00", day + "T12:30:00+09:00")))
				.andExpect(jsonPath("$.data[?(@.startAt == '" + day + "T13:00:00+09:00')].status", contains("ENABLE")));
	}

	@Test
	void brokenMessageMovesToDeadLetterTopicAndLaterMessagesStillApply() {
		send(EventTopicStaticValues.STATION, 400L, "{broken");
		send(EventTopicStaticValues.STATION, 401L, message(50, "StationUpserted", "station", 401,
				station(401, "다음 메시지", "37.3", "127.2")));

		await().atMost(TIMEOUT).until(() -> stationRepository.findById(401L).isPresent());
		try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
				Map.<String, Object>of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
						ConsumerConfig.GROUP_ID_CONFIG, "dlt-check", ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
				new StringDeserializer(), new StringDeserializer()).createConsumer()) {
			String deadLetterTopic = EventTopicStaticValues.STATION + EventTopicStaticValues.DEAD_LETTER_SUFFIX;
			consumer.subscribe(List.of(deadLetterTopic));
			ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, deadLetterTopic, TIMEOUT);
			assertThat(record.value()).isEqualTo("{broken");
			assertThat(Optional.ofNullable(record.key())).contains("400");
		}
	}

	private void send(String topic, Long key, String message) {
		kafkaTemplate.send(topic, String.valueOf(key), message).join();
	}

	private static String message(long version, String eventType, String aggregateType, long aggregateId,
			String payload) {
		return message(version, eventType, aggregateType, aggregateId, payload, Instant.now());
	}

	/** producer OutboxRelay.toMessage와 같은 형식 */
	private static String message(long version, String eventType, String aggregateType, long aggregateId,
			String payload, Instant occurredAt) {
		return "{\"eventId\":" + version + ",\"eventType\":\"" + eventType + "\",\"aggregateType\":\"" + aggregateType
				+ "\",\"aggregateId\":" + aggregateId + ",\"version\":" + version + ",\"occurredAt\":\"" + occurredAt
				+ "\",\"payload\":" + payload + "}";
	}

	private static String station(long id, String name, String latitude, String longitude) {
		return "{\"stationId\":" + id + ",\"name\":\"" + name + "\",\"address\":\"서울특별시 강남구 학동로 426\","
				+ "\"metro\":\"서울특별시\",\"city\":\"강남구\",\"rapidCount\":1,\"slowCount\":1,"
				+ "\"supportedModels\":[\"TESLA\"],\"latitude\":" + latitude + ",\"longitude\":" + longitude + ","
				+ "\"syncedAt\":\"2026-10-15T00:00:00Z\",\"chargers\":["
				+ "{\"chargerId\":" + (id * 10 + 2) + ",\"chargingType\":\"SLOW\",\"maxPowerKw\":7.00,"
				+ "\"status\":\"OUT_OF_SERVICE\",\"dataSource\":\"SIMULATION\"},"
				+ "{\"chargerId\":" + (id * 10 + 1) + ",\"chargingType\":\"FAST\",\"maxPowerKw\":50.00,"
				+ "\"status\":\"AVAILABLE\",\"dataSource\":\"SIMULATION\"}]}";
	}

	private static String reservation(long id, String status, String chargingStartedAt, String completedAt) {
		return "{\"reservationId\":" + id + ",\"userId\":7,\"chargerId\":1011,\"stationId\":101,"
				+ "\"stationName\":\"강남구청 충전소\",\"stationAddress\":\"서울특별시 강남구 학동로 426\","
				+ "\"vehicleModel\":\"TESLA\",\"startsAt\":\"2026-10-15T03:00:00Z\",\"endsAt\":\"2026-10-15T04:00:00Z\","
				+ "\"status\":\"" + status + "\",\"chargingStartedAt\":" + chargingStartedAt + ",\"completedAt\":"
				+ completedAt + "}";
	}

	/** 리스너가 붙기 전에 토픽을 만들어 둔다 */
	@TestConfiguration
	static class Topics {

		@Bean
		NewTopic userTopic() {
			return new NewTopic(EventTopicStaticValues.USER, 1, (short) 1);
		}

		@Bean
		NewTopic stationTopic() {
			return new NewTopic(EventTopicStaticValues.STATION, 1, (short) 1);
		}

		@Bean
		NewTopic reservationTopic() {
			return new NewTopic(EventTopicStaticValues.RESERVATION, 1, (short) 1);
		}

		@Bean
		NewTopic stationDeadLetterTopic() {
			return new NewTopic(EventTopicStaticValues.STATION + EventTopicStaticValues.DEAD_LETTER_SUFFIX, 1, (short) 1);
		}

	}

}
