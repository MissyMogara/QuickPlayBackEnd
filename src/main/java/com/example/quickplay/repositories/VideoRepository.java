package com.example.quickplay.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.quickplay.entities.Video;

import reactor.core.publisher.Mono;

public interface VideoRepository extends ReactiveMongoRepository<Video, String>{
    Mono<Video> findByName(String name);
}
