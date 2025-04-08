package com.example.quickplay.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.entities.Comment;
import com.example.quickplay.entities.Post;
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

    public Mono<ServerResponse> updateComment(ServerRequest request) {
        String commentId = request.pathVariable("commentId");

        return request.bodyToMono(Comment.class)
            .flatMap(updatedComment -> {
                Update update = new Update();

                if(!updatedComment.getText().isEmpty() && updatedComment.getText() != null) {
                    update.set("text", updatedComment.getText());
                }

                return reactiveMongoTemplate.findAndModify(
                    Query.query(Criteria.where("_id").is(commentId)),
                    update,
                    FindAndModifyOptions.options().returnNew(true),
                    Comment.class
                );
            })
            .flatMap(updatedComment -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedComment))
            .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Comment not found"))
            .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("Error updating comment: " + e.getMessage()));
    }

    public Mono<ServerResponse> deleteComment(ServerRequest request) {
        String commentId = request.pathVariable("commentId");

        return reactiveMongoTemplate.remove(
            Query.query(Criteria.where("_id").is(commentId)),
            Comment.class
        )
        .flatMap(deleteResult -> {
            if(deleteResult.getDeletedCount() > 0){
                return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Comment deleted successfully");
            }
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Comment not found");
        });
    }

public Mono<ServerResponse> addCommentToPost(ServerRequest request) {
    String postId = request.pathVariable("postId");
    String commentId = request.pathVariable("commentId");

    return reactiveMongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").is(postId)),
            new Update().addToSet("comments", commentId),
            Post.class
        ).flatMap(updateResult -> {
            if (updateResult.getModifiedCount() > 0) {
                return commentRepository.findById(commentId)
                    .flatMap(updatedComment -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedComment));
            } else {
                return ServerResponse.status(HttpStatus.NOT_FOUND).build();
            }
        }).onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .bodyValue("Error updating the post: " + e.getMessage()));
    }

    public Mono<ServerResponse> removeCommentFromPost(ServerRequest request) {
        String postId = request.pathVariable("postId");
        String commentId = request.pathVariable("commentId");
    
        return reactiveMongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").is(postId)),
            new Update().pull("comments", commentId),
            Post.class 
        ).flatMap(updateResult -> {

            if (updateResult.getModifiedCount() > 0) {
                return reactiveMongoTemplate.remove(
                    Query.query(Criteria.where("_id").is(commentId)),
                    Comment.class
                ).flatMap(deleteResult -> {
                    if (deleteResult.getDeletedCount() > 0) {
                        return ServerResponse.noContent().build();
                    } else {
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                            .bodyValue("Comment not found or couldn't be deleted.");
                    }
                });
            } else {
                return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .bodyValue("Comment not found in post.");
            }
        }).onErrorResume(e -> {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Error occurred: " + e.getMessage());
        });
    }

    public Mono<ServerResponse> addCommentToComment(ServerRequest request) {
        String commentId = request.pathVariable("commentId");
        String replyId = request.pathVariable("replyId");
    
        return reactiveMongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").is(commentId)),
            new Update().addToSet("replies", replyId),
            Comment.class
        ).flatMap(updateResult -> {
            if (updateResult.getModifiedCount() > 0) {
                return commentRepository.findById(replyId)
                    .flatMap(updatedComment -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedComment));
            } else {
                return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .bodyValue("Comment not found or could not be updated.");
            }
        }).onErrorResume(e -> {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Error occurred while adding reply: " + e.getMessage());
        });
    }

    public Mono<ServerResponse> removeCommentFromComment(ServerRequest request) {
        String commentId = request.pathVariable("commentId");
        String replyId = request.pathVariable("replyId");
    
        return reactiveMongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").is(commentId)),
            new Update().pull("replies", replyId),
            Comment.class
        ).flatMap(updateResult -> {
            if (updateResult.getModifiedCount() > 0) {
                return reactiveMongoTemplate.remove(
                    Query.query(Criteria.where("_id").is(replyId)),
                    Comment.class
                ).flatMap(deleteResult -> {
                    if (deleteResult.getDeletedCount() > 0) {
                        return ServerResponse.noContent().build();
                    } else {
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                            .bodyValue("Reply not found or couldn't be deleted.");
                    }
                });
            } else {
                return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .bodyValue("Reply not found in the parent comment.");
            }
        }).onErrorResume(e -> {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Error occurred while removing reply: " + e.getMessage());
        });
    }

}
