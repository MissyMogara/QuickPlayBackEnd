package com.example.quickplay.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.entities.Project;
import com.example.quickplay.repositories.ProjectRepository;

import reactor.core.publisher.Mono;

@Component
public class ProjectHandler {
    // TODO: SI BORRAS UN PROYECTO TAMBIEN BORRAS LOS POSTS, SI BORRAS UN USUARIO TAMBIEN BORRAS LOS PROYECTOS
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    private final ProjectRepository projectRepository;

    public ProjectHandler(ProjectRepository projectRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.projectRepository = projectRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<ServerResponse> createProject(ServerRequest request) {
        return request.bodyToMono(Project.class)
                .flatMap(project -> projectRepository.save(project))
                .flatMap(savedProject -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedProject))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Error creating project: " + e.getMessage()));
    }
}
