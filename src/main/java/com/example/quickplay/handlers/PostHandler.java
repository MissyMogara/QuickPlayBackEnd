package com.example.quickplay.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.entities.Post;
import com.example.quickplay.repositories.PostRepository;

import reactor.core.publisher.Mono;

@Component
public class PostHandler {

    @Autowired
    private final PostRepository postRepository;

    public PostHandler(PostRepository postRepository) {
        this.postRepository = postRepository;
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
        return postRepository.findById(postId)
                .flatMap(post -> {
                    post.setLikes(post.getLikes() + 1); // Increment likes
                    return postRepository.save(post);
                })
                .flatMap(updatedPost -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedPost))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Post not found"));
        }



}
