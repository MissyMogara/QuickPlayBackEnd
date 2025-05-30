package com.example.quickplay.entities;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "videos")
public class Video {

    @Id
    private String id;

    @Indexed(unique = true)  
    private String name;
    
    private String url;

    private String title;
    
    private String createdAt;

    private List<String> segments;

    // Constructor sin argumentos
    public Video() {
    }

    // Constructor con argumentos
    public Video(String id, String name, String url, String title, String createdAt, List<String> segments) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.title = title;
        this.createdAt = createdAt;
        this.segments = segments;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getSegments() {
        return segments;
    }

    public void setSegments(List<String> segments) {
        this.segments = segments;
    }
    
}
