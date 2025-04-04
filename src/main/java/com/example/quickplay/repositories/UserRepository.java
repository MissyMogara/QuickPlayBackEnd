package com.example.quickplay.repositories;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.quickplay.entities.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
}