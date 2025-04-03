package com.example.quickplay.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "posts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
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
}
