package com.example.quickplay.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "projects")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Project {
    @Id
    private String id;

    private String userId;

    @Indexed(unique = true)
    private String name; 
    private String description; 
    private List<String> posts; // Id de los posts
    private List<String> followers; // Id de los seguidores
    private String createdAt;
    
}
