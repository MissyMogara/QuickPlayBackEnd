package com.example.quickplay.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.quickplay.models.Users;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<Users, String> {
    Mono<Users> findByUsername(String username);

    Mono<Users> findByEmail(String email);
    
}
