package com.example.quickplay.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.quickplay.entities.Project;

import reactor.core.publisher.Mono;

public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {
    Mono<Project> findByUserId(String userId);
    Mono<Void> deleteByUserId(String userId);
}
