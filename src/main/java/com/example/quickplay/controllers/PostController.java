package com.example.quickplay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quickplay.models.Posts;
import com.example.quickplay.services.PostService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts") 
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public Mono<ResponseEntity<String>> createPost(@RequestBody Posts post) {
        return postService.createPost(post)
            .map(savedPost -> ResponseEntity.status(HttpStatus.CREATED).body("Post created successfully"))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.badRequest().body(e.getMessage())
            ));
    }

    @GetMapping("/{postId}")
    public Mono<ResponseEntity<Posts>> getPostById(@PathVariable("postId") String postId) {
        System.out.println("Fetching post with ID: " + postId);
        return postService.getPostById(postId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume(e -> {
                System.out.println("Error occurred: " + e.getMessage());
                return Mono.just(ResponseEntity.badRequest().body(null));
            });
    }

    @GetMapping("/user/{userId}")
    public Flux<Posts> getPostsByUserId(@PathVariable String userId) {
        return postService.getPostsByUserId(userId);
    }

    @GetMapping("/search")
    public Flux<Posts> getPostByTitle(@RequestParam String title) {
        return postService.getPostByTitle(title);
    }

    @PutMapping("/{postId}/like")
    public Mono<ResponseEntity<String>> incrementLikes(@PathVariable String postId) {
        return postService.incrementLikes(postId)
            .map(post -> ResponseEntity.ok("Likes incremented successfully"))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{postId}/unlike")
    public Mono<ResponseEntity<String>> decrementLikes(@PathVariable String postId) {
        return postService.decrementLikes(postId)
            .map(post -> ResponseEntity.ok("Likes decremented successfully"))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{postId}/view")
    public Mono<ResponseEntity<String>> incrementViews(@PathVariable String postId) {
        return postService.incrementViews(postId)
            .map(post -> ResponseEntity.ok("Views incremented successfully"))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{postId}")
    public Mono<ResponseEntity<Posts>> updatePost(
            @PathVariable String postId, 
            @RequestBody Posts updatedPost) {
        return postService.updatePost(postId, updatedPost)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{postId}")
    public Mono<ResponseEntity<Object>> deletePost(@PathVariable String postId) {
        return postService.deletePost(postId)
            .then(Mono.defer(() -> Mono.just(ResponseEntity.noContent().build())))
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{postId}/comments")
    public Mono<ResponseEntity<Posts>> addComment(
            @PathVariable String postId, 
            @RequestBody String comment) {
        return postService.addComment(postId, comment)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}