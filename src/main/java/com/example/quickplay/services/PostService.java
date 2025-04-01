package com.example.quickplay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.quickplay.models.Posts;
import com.example.quickplay.repositories.PostRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    // Create a new post
    public Mono<Posts> createPost(Posts post) {

        if (post.getTitle() == null || post.getTitle().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Title is required"));
        }

        if (post.getContent() == null || post.getContent().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Content is required"));
        }

        if (post.getVideoUrls() == null || post.getVideoUrls().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Video URLs are required"));
        }

        post.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        post.setLikes(0);
        post.setViews(0);
        return postRepository.save(post);
    }

    // Get a post by ID
    public Mono<Posts> getPostById(String id) {
        System.out.println("Searching post by ID: " + id);
        return postRepository.findById(id)
        .doOnNext(post -> System.out.println("Post found: " + post))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found")));
    }

    // Get all posts by user ID
    public Flux<Posts> getPostsByUserId(String userId) {
        return postRepository.findAllByUserId(userId).switchIfEmpty(Mono.error(new IllegalArgumentException("Posts not found")));
    }

    // Get post by title
    public Flux<Posts> getPostByTitle(String title) {
        return postRepository.findByTitle(title).switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found")));
    }

    // Increment likes for a post
    public Mono<Posts> incrementLikes(String postId) {
        return postRepository.findById(postId)
                .flatMap(post -> {
                    post.setLikes(post.getLikes() + 1);
                    return postRepository.save(post);
                });
    }

    // Decrement likes for a post
    public Mono<Posts> decrementLikes(String postId) {
        
        return postRepository.findById(postId)
                .flatMap(post -> {
                    if (post.getLikes() > 0) {
                        post.setLikes(post.getLikes() - 1);
                    } else {
                        return Mono.error(new IllegalArgumentException("Likes cannot be less than 0"));
                    }
                    return postRepository.save(post);
                });
    }

    // Increment views for a post
    public Mono<Posts> incrementViews(String postId) {
        return postRepository.findById(postId)
                .flatMap(post -> {
                    post.setViews(post.getViews() + 1);
                    return postRepository.save(post);
                });
    }

    // Update a post
    public Mono<Posts> updatePost(String id, Posts updatedPost) {

        return postRepository.findById(id)
                .flatMap(post -> {
                    if (updatedPost.getTitle() != null) {
                        post.setTitle(updatedPost.getTitle());
                    }
                    if (updatedPost.getContent() != null) {
                        post.setContent(updatedPost.getContent());
                    }
                    if (updatedPost.getVideoUrls() != null) {
                        post.setVideoUrls(updatedPost.getVideoUrls());
                    }
                    if (updatedPost.getComments() != null) {
                        post.setComments(updatedPost.getComments());
                    }
                    return postRepository.save(post);
                });
    }

    // Delete a post
    public Mono<Void> deletePost(String id) {
        return postRepository.deleteById(id);
    }

    // Add a comment to a post
    public Mono<Posts> addComment(String postId, String comment) {
        return postRepository.findById(postId)
                .flatMap(post -> {
                    post.getComments().add(comment);
                    return postRepository.save(post);
                });
    }
        
}
