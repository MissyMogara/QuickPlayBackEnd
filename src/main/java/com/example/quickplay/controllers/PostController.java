package com.example.quickplay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.quickplay.services.PostService;
import com.example.quickplay.models.Posts; 

import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
public class PostController {

    @Autowired
    private PostService postService;

    // Create a new post
    @PostMapping("/posts")
    public Mono<ResponseEntity<String>> createPost(@RequestBody Posts post) {
        return postService.createPost(post)
            .map(savedPost -> ResponseEntity.status(HttpStatus.CREATED).body("Post created successfully"))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.badRequest().body(e.getMessage())
            )); 
    }

    // Get all posts by post ID
    @GetMapping("/posts/{postId}")
    public Mono<ResponseEntity<String>> getPostById(@RequestParam String postId) {
        return postService.getPostById(postId)
            .map(post -> ResponseEntity.ok(post.toString()))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            )); 
    }

    // Get all posts by user ID
    @GetMapping("/posts/user/{userId}")
    public Mono<ResponseEntity<String>> getPostsByUserId(@RequestParam String userId) {
        return postService.getPostsByUserId(userId)
            .collectList()
            .map(posts -> ResponseEntity.ok(posts.toString()))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            )); 
    }

    // Get post by title
    @GetMapping("/posts/title/{title}")
    public Mono<ResponseEntity<String>> getPostByTitle(@RequestParam String title) {
        return postService.getPostByTitle(title)
            .collectList()
            .map(posts -> ResponseEntity.ok(posts.toString()))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            )); 
    }

    // Increment likes for a post
    @PutMapping("/posts/{postId}/like")
    public Mono<ResponseEntity<String>> incrementLikes(@RequestParam String postId) {
        return postService.incrementLikes(postId)
            .map(post -> ResponseEntity.ok("Likes incremented successfully"))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            ));
    }

    // Decrement likes for a post
    @PutMapping("/posts/{postId}/decrement")
    public Mono<ResponseEntity<String>> decrementLikes(@RequestParam String postId) {
        return postService.decrementLikes(postId)
            .map(post -> ResponseEntity.ok("Likes decremented successfully"))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            ));
    }

    // Increment views for a post
    @PutMapping("/posts/{postId}/view")
    public Mono<ResponseEntity<String>> incrementViews(@RequestParam String postId) {
        return postService.incrementViews(postId)
            .map(post -> ResponseEntity.ok("Views incremented successfully"))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            ));
    }

    // Update a post
    @PutMapping("/posts/{postId}")
    public Mono<ResponseEntity<String>> updatePost(@RequestParam String postId, @RequestBody Posts updatedPost) {
        return postService.updatePost(postId, updatedPost)
            .map(post -> ResponseEntity.ok("Post updated successfully"))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            ));
    }

    // Delete a post
    @DeleteMapping("/posts/{postId}")
    public Mono<ResponseEntity<String>> deletePost(@RequestParam String postId) {
        return postService.deletePost(postId)
            .map(post -> ResponseEntity.ok("Post deleted successfully"))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            ));
    }

    // Add a comment to a post
    @PutMapping("/posts/{postId}/comments")
    public Mono<ResponseEntity<String>> addComment(@RequestParam String postId, @RequestBody String comment) {
        return postService.addComment(postId, comment)
            .map(post -> ResponseEntity.ok("Comment added successfully"))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())
            ));
    }
    
    
    
}
