package com.example.quickplay.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.quickplay.entities.Comment;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findAllByUserId(String userId);
    Flux<Comment> findAllByUserIdOrderByLikesDesc(String userId);
    Mono<Void> deleteByUserId(String userId);
}
