package com.picka_chew.dangcharge_Consumer.domain.common.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReplaceOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.picka_chew.dangcharge_Consumer.domain.common.entity.EventVersionedDocument;

import lombok.RequiredArgsConstructor;

/**
 * "같은 id이고 저장된 version이 더 작다"를 조건으로 upsert한다. 조건에 맞는 문서가 없으면 새로 넣으려 하는데,
 * 같은 id의 더 새 문서가 이미 있으면 _id 중복으로 실패하므로 오래된 이벤트로 보고 건너뛴다.
 */
@RequiredArgsConstructor
public class EventVersionedRepositoryImpl<T extends EventVersionedDocument> implements EventVersionedRepository<T> {

	private final MongoOperations mongoOperations;

	@Override
	public boolean replaceIfNewer(T document) {
		Query query = Query.query(Criteria.where("_id").is(document.getId()).orOperator(
				Criteria.where(EventVersionedDocument.EVENT_VERSION).lt(document.getEventVersion()),
				Criteria.where(EventVersionedDocument.EVENT_VERSION).exists(false)));
		try {
			mongoOperations.replace(query, document, ReplaceOptions.replaceOptions().upsert(),
					mongoOperations.getCollectionName(document.getClass()));
			return true;
		}
		catch (DuplicateKeyException exception) {
			return false;
		}
	}

}
