package com.example.quickplay.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "posts")
public class Posts {
    
    @Id
    private String id;
    private String title;
    private String content;
    private List<String> videoUrls;
    private String userId; 
    private Integer likes;
    private Integer views;
    private List<String> comments; // id comments
    private String createdAt;

    @Override
    public String toString() {
        return "Posts{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId='" + userId + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }


}
