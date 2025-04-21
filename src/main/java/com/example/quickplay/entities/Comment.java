package com.example.quickplay.entities;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "comments")
public class Comment {
    @Id
    private String id;

    private String userId;

    private String text;

    private List<String> replies;  // Id comentarios

    private Integer likes;

    private String createdAt;

     // Constructor sin argumentos
     public Comment() {
    }

    // Constructor con argumentos
    public Comment(String id, String userId, String text, List<String> replies, Integer likes, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.replies = replies;
        this.likes = likes;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getReplies() {
        return replies;
    }

    public void setReplies(List<String> replies) {
        this.replies = replies;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

}
