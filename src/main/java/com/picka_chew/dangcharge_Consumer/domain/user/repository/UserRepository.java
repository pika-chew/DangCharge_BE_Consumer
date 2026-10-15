package com.picka_chew.dangcharge_Consumer.domain.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.picka_chew.dangcharge_Consumer.domain.common.repository.EventVersionedRepository;
import com.picka_chew.dangcharge_Consumer.domain.user.entity.User;

public interface UserRepository extends MongoRepository<User, Long>, EventVersionedRepository<User> {
}
