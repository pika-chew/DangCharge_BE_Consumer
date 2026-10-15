package com.picka_chew.dangcharge_Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// MongoDB·Kafka 없이 기동만 확인한다. 인덱스 생성(MongoDB 접속)과 리스너 시작(Kafka 접속)을 끈다
@SpringBootTest(properties = {"spring.data.mongodb.auto-index-creation=false",
		"spring.kafka.listener.auto-startup=false"})
class DangchargeConsumerApplicationTests {

	@Test
	void contextLoads() {
	}

}
