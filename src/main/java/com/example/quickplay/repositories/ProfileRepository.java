package com.example.quickplay.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.quickplay.entities.Profile;

import reactor.core.publisher.Mono;

public interface ProfileRepository extends ReactiveMongoRepository<Profile, String> {
    Mono<Profile> findByUserId(String userId);
    Mono<Void> deleteByUserId(String userId);
}
