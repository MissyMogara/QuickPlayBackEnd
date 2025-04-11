package com.example.quickplay.handlers;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.repositories.PostRepository;

import reactor.core.publisher.Mono;

@Component
public class VideoHandler {
    // HAY QUE HACER QUE EL ENLACE QUE CREA LO META EN UN POST
    private static final String VIDEO_DIRECTORY = "src/main/resources/videos/";

    private final PostRepository postRepository;

    public VideoHandler(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Mono<ServerResponse> uploadVideo(ServerRequest request) {
        return request.multipartData()
                .flatMap(parts -> {
                    FilePart filePart = (FilePart) parts.get("file").get(0);
                    String customFileName = ((FormFieldPart) parts.get("fileName").get(0)).value();
                    String fileExtension = filePart.filename().substring(filePart.filename().lastIndexOf("."));
                    String fullPath = VIDEO_DIRECTORY + customFileName + fileExtension;
                    File videoFile = new File(fullPath);
    
                    return filePart.transferTo(videoFile)
                            .flatMap(v -> {
                                return ServerResponse.status(HttpStatus.CREATED)
                                    .bodyValue("Video subido con éxito: " + fullPath);
                            });
                })
                .onErrorResume(e -> {
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al subir el video: " + e.getMessage());
                });
    }

    public Mono<ServerResponse> addVideoToPost(ServerRequest request) {
        // Obtener los Path Variables
        String postId = request.pathVariable("postId");
        String videoName = request.pathVariable("videoName");
    
        // Validación de los parámetros
        if (postId == null || videoName == null) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .bodyValue("Se requiere el postId y el videoName.");
        }
    
        return postRepository.findById(postId)
                .flatMap(post -> {
                    post.getVideoUrls().add(videoName);
                    return postRepository.save(post)
                            .flatMap(updatedPost -> {
                                return ServerResponse.status(HttpStatus.OK)
                                        .bodyValue("Video añadido al Post exitosamente: " + videoName);
                            });
                })
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue("No se encontró el Post con el ID proporcionado: " + postId))
                .onErrorResume(e -> {
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al añadir el video al Post: " + e.getMessage());
                });
    }

    public Mono<ServerResponse> getVideoByName(ServerRequest request) {
        String videoName = request.pathVariable("videoName");
        File videoFile = new File(VIDEO_DIRECTORY + videoName);

        if (!videoFile.exists()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new FileSystemResource(videoFile);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(resource);
    }
}
