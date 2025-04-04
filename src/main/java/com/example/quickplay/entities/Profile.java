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

@Document(collection = "profiles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
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
}
