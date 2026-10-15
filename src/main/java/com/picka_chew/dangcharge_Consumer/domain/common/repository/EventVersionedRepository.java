package com.picka_chew.dangcharge_Consumer.domain.common.repository;

import com.picka_chew.dangcharge_Consumer.domain.common.entity.EventVersionedDocument;

/** 이벤트 스냅샷으로 문서를 통째로 바꾸는 리포지토리 조각. 문서별 리포지토리가 함께 상속한다 */
public interface EventVersionedRepository<T extends EventVersionedDocument> {

	/**
	 * 저장된 문서보다 새 이벤트일 때만 문서를 바꾸고, 없으면 만든다.
	 * 같은 이벤트를 다시 받거나 순서가 뒤바뀌어 오래된 이벤트가 늦게 와도 최신 상태가 덮이지 않는다.
	 *
	 * @return 반영했으면 true, 이미 같거나 더 새 이벤트를 반영한 상태라 건너뛰었으면 false
	 */
	boolean replaceIfNewer(T document);

}
