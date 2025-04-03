package com.example.quickplay.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.data.mongodb.core.query.Query;

import com.example.quickplay.entities.Comment;
import com.example.quickplay.repositories.CommentRepository;

import reactor.core.publisher.Mono;

@Component
public class CommentHandler {
    
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    private final CommentRepository commentRepository;
    
    public CommentHandler(CommentRepository commentRepository, 
    ReactiveMongoTemplate reactiveMongoTemplate) {
        this.commentRepository = commentRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<ServerResponse> createComment(ServerRequest request) {
        return request.bodyToMono(Comment.class)
        .map(comment -> {
            comment.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            return comment;
        })
        .flatMap(comment -> commentRepository.save(comment))
        .flatMap(savedComment -> 
            ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(savedComment)
        )
        .onErrorResume(e -> 
            ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Error creating comment: " + e.getMessage())
        );
    }

    public Mono<ServerResponse> getCommentById(ServerRequest request) {
        String commentId = request.pathVariable("commentId");
        return commentRepository.findById(commentId)
        .flatMap(comment -> ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(comment))
        .switchIfEmpty(
            ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Comment not found")
        );
    }
    
    public Mono<ServerResponse> getCommentsByUserId(ServerRequest request) {
        String userId = request.pathVariable("userId");
        return commentRepository.findAllByUserId(userId)
        .collectList()
        .flatMap(comment -> ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(comment))
        .switchIfEmpty(
            ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("User doesn't have comments")
        );
    }

    public Mono<ServerResponse> getCommentsByUserIdOrderByLikes(ServerRequest request) {
        String userId = request.pathVariable("userId");
        return commentRepository.findAllByUserIdOrderByLikesDesc(userId)
        .collectList()
        .flatMap(comment -> ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(comment))
        .switchIfEmpty(
            ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("User doesn't have comments")
        );
    }
    
    public Mono<ServerResponse> likeComment(ServerRequest request) {
        String commentId = request.pathVariable("commentId");
        
        return reactiveMongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").is(commentId)),
            new Update().inc("likes", 1),
            Comment.class
        )
        .flatMap(updateResult -> {
            if (updateResult.getModifiedCount() > 0) {
                return commentRepository.findById(commentId)
                    .flatMap(updatedComment -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedComment));
            }
            return commentRepository.existsById(commentId)
                .flatMap(exists -> exists ?
                    ServerResponse.status(HttpStatus.BAD_REQUEST)
                        .bodyValue("Comment already has minimum likes") :
                    ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue("Comment not found"));
        });
    }

    public Mono<ServerResponse> unlikeComment(ServerRequest request) {
        String commentId = request.pathVariable("commentId");
        
        return reactiveMongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").is(commentId).and("likes").gt(0)),
            new Update().inc("likes", -1),
            Comment.class
        )
        .flatMap(updateResult -> {
            if (updateResult.getModifiedCount() > 0) {
                return commentRepository.findById(commentId)
                    .flatMap(updatedComment -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedComment));
            }
            return commentRepository.existsById(commentId)
                .flatMap(exists -> exists ?
                    ServerResponse.status(HttpStatus.BAD_REQUEST)
                        .bodyValue("Comment can't be unliked") :
                    ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue("Comment not found"));
        });
    }

}
