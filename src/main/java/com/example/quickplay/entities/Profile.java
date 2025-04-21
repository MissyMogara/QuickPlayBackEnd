package com.example.quickplay.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "profiles")
public class Profile {
    @Id
    private String id;

    @Indexed(unique = true) 
    private String userId;
    private List<String> projects; // Id de los proyectos
    private List<String> followers; // Id de los seguidores
    private String bio;
    private String profilePictureUrl;
    private String createdAt;

    // Constructor sin argumentos
    public Profile() {
    }

    // Constructor con argumentos
    public Profile(String id, String userId, List<String> projects, List<String> followers, String bio, String profilePictureUrl, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.projects = projects;
        this.followers = followers;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
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

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
