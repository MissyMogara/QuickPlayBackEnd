package com.example.quickplay.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.quickplay.models.Posts;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface PostRepository extends ReactiveMongoRepository<Posts, String> {
    Mono<Posts> findByUserId(String userId);
    Flux<Posts> findAllByUserId(String userId); 
    Flux<Posts> findByTitle(String title);
    
}
