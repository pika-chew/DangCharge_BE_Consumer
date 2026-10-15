package com.picka_chew.dangcharge_Consumer.infra.kafka.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import com.picka_chew.dangcharge_Consumer.common.consts.EventTopicStaticValues;

import tools.jackson.core.JacksonException;

/**
 * 리스너 오류 처리. 같은 메시지를 1초부터 최대 30초 간격으로 5분 동안 다시 시도하고(MongoDB 일시 장애 대비),
 * 그래도 실패하면 {토픽}.DLT로 옮기고 다음 메시지로 넘어간다. JSON 형식이 깨진 메시지는 다시 시도해도 같으니 바로 옮긴다.
 * 재시도하는 동안에는 같은 파티션의 뒤 메시지를 처리하지 않아 이벤트 순서가 유지된다.
 */
@Configuration
public class KafkaConsumerConfig {

	private static final long INITIAL_INTERVAL_MILLIS = 1_000;
	private static final long MAX_INTERVAL_MILLIS = 30_000;
	private static final long MAX_ELAPSED_MILLIS = 300_000;

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
				(record, exception) -> new TopicPartition(record.topic() + EventTopicStaticValues.DEAD_LETTER_SUFFIX, -1));
		ExponentialBackOff backOff = new ExponentialBackOff(INITIAL_INTERVAL_MILLIS, 2);
		backOff.setMaxInterval(MAX_INTERVAL_MILLIS);
		backOff.setMaxElapsedTime(MAX_ELAPSED_MILLIS);
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
		errorHandler.addNotRetryableExceptions(JacksonException.class);
		return errorHandler;
	}

}
