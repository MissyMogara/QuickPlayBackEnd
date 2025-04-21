package com.example.quickplay.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projects")
public class Project {
    @Id
    private String id;

    private String userId;

    @Indexed(unique = true)
    private String name; 
    private String description; 
    private String image; // URL de la imagen
    private List<String> posts; // Id de los posts
    private List<String> followers; // Id de los seguidores
    private String createdAt;

    // Constructor sin argumentos
    public Project() {
    }

    // Constructor con argumentos
    public Project(String id, String userId, String name, String description, String image, List<String> posts, List<String> followers, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.image = image;
        this.posts = posts;
        this.followers = followers;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getPosts() {
        return posts;
    }

    public void setPosts(List<String> posts) {
        this.posts = posts;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
}
