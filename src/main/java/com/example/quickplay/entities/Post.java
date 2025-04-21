package com.example.quickplay.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "posts")
public class Post {
    @Id
    private String id;
    private String title;
    private String content;
    private List<String> videoUrls; // Lista de URLs de videos
    private String userId; // ID del usuario que creó el post
    private Integer likes; // Número de "me gusta"
    private Integer views;
    private List<String> comments; // Lista de del id de los comentarios
    private String createdAt; // Fecha de creación del post

    // Constructor sin parámetros
    public Post() {
    }

    // Constructor con parámetros
    public Post(String id, String title, String content, List<String> videoUrls, String userId, Integer likes, Integer views, List<String> comments, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.videoUrls = videoUrls;
        this.userId = userId;
        this.likes = likes;
        this.views = views;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
