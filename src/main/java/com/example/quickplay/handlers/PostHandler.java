package com.example.quickplay.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

import com.example.quickplay.entities.Post;
import com.example.quickplay.repositories.PostRepository;

import reactor.core.publisher.Mono;

@Component
public class PostHandler {

        private final ReactiveMongoTemplate reactiveMongoTemplate;

        @Autowired
        private final PostRepository postRepository;

        public PostHandler(PostRepository postRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
                this.postRepository = postRepository;
                this.reactiveMongoTemplate = reactiveMongoTemplate;
        }

        public Mono<ServerResponse> createPost(ServerRequest request) {
        return request.bodyToMono(Post.class)
                .map(post -> {
                        post.setCreatedAt(String.valueOf(System.currentTimeMillis())); // Set the current timestamp
                        return post;
                })
                .flatMap(post -> postRepository.save(post))
                .flatMap(savedPost -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedPost))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Error creating post: " + e.getMessage()));
        }

        public Mono<ServerResponse> getPostById(ServerRequest request) {
                String postId = request.pathVariable("postId");
                return postRepository.findById(postId)
                        .flatMap(post -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(post))
                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("Post not found"));
        }

        public Mono<ServerResponse> getPostsByUserId(ServerRequest request) {
        String userId = request.pathVariable("userId");
        return postRepository.findAllByUserId(userId)
                .collectList()
                .flatMap(posts -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(posts))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("No posts found for user ID: " + userId));
        }

        public Mono<ServerResponse> getPostByTitle(ServerRequest request) {
        String title = request.queryParam("title").orElse("");
        return postRepository.findByTitle(title)
                .collectList()
                .flatMap(posts -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(posts))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("No posts found with title: " + title));
        }

        public Mono<ServerResponse> likePost(ServerRequest request) {
                String postId = request.pathVariable("postId");
                
                return reactiveMongoTemplate.findAndModify(
                    Query.query(Criteria.where("_id").is(postId)),
                    new Update().inc("likes", 1),
                    FindAndModifyOptions.options().returnNew(true),
                    Post.class
                )
                .flatMap(updatedPost -> ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updatedPost))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("Post not found"));
        }

        public Mono<ServerResponse> unlikePost(ServerRequest request) {
                String postId = request.pathVariable("postId");
                
                return reactiveMongoTemplate.findAndModify(
                        Query.query(Criteria.where("_id").is(postId).and("likes").gt(0)),
                        new Update().inc("likes", -1),
                        FindAndModifyOptions.options().returnNew(true),
                        Post.class
                )
                .flatMap(updatedPost -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedPost))
                .switchIfEmpty(
                        postRepository.existsById(postId)
                        .flatMap(exists -> exists ?
                                ServerResponse.status(HttpStatus.BAD_REQUEST)
                                .bodyValue("Post can't be unliked") :
                                ServerResponse.status(HttpStatus.NOT_FOUND)
                                .bodyValue("Post not found"))
                );
        }


        public Mono<ServerResponse> incrementViews(ServerRequest request) {
                String postId = request.pathVariable("postId");
                
                return reactiveMongoTemplate.findAndModify(
                    Query.query(Criteria.where("_id").is(postId)),
                    new Update().inc("views", 1),
                    FindAndModifyOptions.options().returnNew(true),
                    Post.class
                )
                .flatMap(updatedPost -> ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updatedPost))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("Post not found"));
        }

        public Mono<ServerResponse> updatePost(ServerRequest request) {
                String postId = request.pathVariable("postId");
                
                return request.bodyToMono(Post.class)
                    .flatMap(updatedPost -> {
                        Update update = new Update();
                        
                        if(updatedPost.getTitle() != null && !updatedPost.getTitle().isEmpty()) {
                            update.set("title", updatedPost.getTitle());
                        }
                        if(updatedPost.getContent() != null && !updatedPost.getContent().isEmpty()) {
                            update.set("content", updatedPost.getContent());
                        }
                        if(updatedPost.getVideoUrls() != null && !updatedPost.getVideoUrls().isEmpty()) {
                            update.set("videoUrls", updatedPost.getVideoUrls());
                        }
                        
                        return reactiveMongoTemplate.findAndModify(
                            Query.query(Criteria.where("_id").is(postId)),
                            update,
                            FindAndModifyOptions.options().returnNew(true),
                            Post.class
                        );
                    })
                    .flatMap(updatedPost -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedPost))
                    .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Post not found"))
                    .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Error updating post: " + e.getMessage()));
        }

        public Mono<ServerResponse> deletePost(ServerRequest request) {
                String postId = request.pathVariable("postId");
                return postRepository.findById(postId)
                        .flatMap(post -> postRepository.delete(post)
                                .then(ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue("Post deleted successfully")))
                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("Post not found"));
        }


}
