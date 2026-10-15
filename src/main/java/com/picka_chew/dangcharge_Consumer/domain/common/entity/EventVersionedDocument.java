package com.picka_chew.dangcharge_Consumer.domain.common.entity;

/**
 * producer 이벤트로 만든 문서. id는 producer DB의 ID를 그대로 쓴다.
 * eventVersion은 마지막으로 반영한 이벤트의 version(producer outbox id, 단조 증가)이다.
 */
public interface EventVersionedDocument {

	String EVENT_VERSION = "eventVersion";

	Long getId();

	long getEventVersion();

}
