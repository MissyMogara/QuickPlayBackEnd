package com.example.quickplay.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.entities.Post;
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

    public Mono<ServerResponse> getProjectById(ServerRequest request) {
        String projectId = request.pathVariable("projectId");

        return projectRepository.findById(projectId)
                .flatMap(project -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(project))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Project not found"));
    }

    public Mono<ServerResponse> getProjectsByUserId(ServerRequest request) {
        String userId = request.pathVariable("userId");

        return projectRepository.findByUserId(userId)
                .flatMap(project -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(project))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Project not found"));
    }

    public Mono<ServerResponse> addPostToProject(ServerRequest request) {
        String postId = request.pathVariable("postId");
        String projectId = request.pathVariable("projectId");

        return reactiveMongoTemplate.findAndModify(
                Query.query(Criteria.where("_id").is(projectId)),
                new Update().addToSet("posts", postId),
                FindAndModifyOptions.options().returnNew(true),
                Project.class
        )
        .flatMap(updatedPost -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedPost))
        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Post not found"))
        .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Error following post: " + e.getMessage()));
        }

        public Mono<ServerResponse> removePostFromProject(ServerRequest request) {
            String postId = request.pathVariable("postId");
            String projectId = request.pathVariable("projectId");

            return reactiveMongoTemplate.findAndModify(
                    Query.query(Criteria.where("_id").is(projectId)),
                    new Update().pull("posts", postId),
                    FindAndModifyOptions.options().returnNew(true),
                    Project.class
            )
            .flatMap(updatedPost -> ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updatedPost))
            .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("Post not found"))
            .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("Error unfollowing post: " + e.getMessage()));
        }

    public Mono<ServerResponse> updateProject(ServerRequest request) {
        String projectId = request.pathVariable("projectId");

        return request.bodyToMono(Project.class)
                .flatMap(project -> projectRepository.findById(projectId)
                        .flatMap(existingProject -> {
                            if (project.getName() != null && !project.getName().equals(existingProject.getName()) && project.getName().length() > 0) {
                                existingProject.setName(project.getName());
                            }
                            if (project.getDescription() != null && !project.getDescription().equals(existingProject.getDescription()) && project.getDescription().length() > 0) {
                                existingProject.setDescription(project.getDescription());
                            }
                            if (project.getImage() != null && !project.getImage().equals(existingProject.getImage()) && project.getImage().length() > 0) {
                                existingProject.setImage(project.getImage());
                            }
                            return projectRepository.save(existingProject);
                        }))
                .flatMap(updatedProject -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedProject))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Project not found"));
    }

    public Mono<ServerResponse> deleteProject(ServerRequest request) {
        String projectId = request.pathVariable("projectId");

        // Busca el proyecto para obtener el array de posts asociados
        return reactiveMongoTemplate.findOne(
                Query.query(Criteria.where("_id").is(projectId)),
                Project.class
        )
        .flatMap(project -> {
            if (project != null) {
                // Borra todos los posts asociados al proyecto
                return reactiveMongoTemplate.remove(
                        Query.query(Criteria.where("_id").in(project.getPosts())),
                        Post.class
                )
                .then(
                    // Borra el proyecto después de eliminar los posts
                    reactiveMongoTemplate.remove(
                            Query.query(Criteria.where("_id").is(projectId)),
                            Project.class
                    )
                )
                .flatMap(deleteResult -> {
                    if (deleteResult.getDeletedCount() > 0) {
                        return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("Project and associated posts deleted successfully");
                    }
                    return ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Project not found");
                });
            } else {
                return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("Project not found");
            }
        })
        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Project not found"))
        .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Error deleting project and posts: " + e.getMessage()));
    }
}
