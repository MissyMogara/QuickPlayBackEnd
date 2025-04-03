package com.example.quickplay.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.quickplay.entities.Post;

import reactor.core.publisher.Flux;


public interface PostRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findAllByUserId(String userId); 
    Flux<Post> findByTitle(String title);
    
}
