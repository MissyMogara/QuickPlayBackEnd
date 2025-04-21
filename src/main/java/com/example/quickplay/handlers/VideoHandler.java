package com.example.quickplay.handlers;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.entities.Video;
import com.example.quickplay.repositories.PostRepository;
import com.example.quickplay.repositories.VideoRepository;

import reactor.core.publisher.Mono;

@Component
public class VideoHandler {
    private static final String VIDEO_DIRECTORY = "src/main/resources/videos/";
    

    private final PostRepository postRepository;

    private final VideoRepository videoRepository;

    public VideoHandler(PostRepository postRepository, VideoRepository videoRepository) {
        this.postRepository = postRepository;
        this.videoRepository = videoRepository;
    }

    
    public Mono<ServerResponse> uploadVideo(ServerRequest request) {
    return request.multipartData()
                .flatMap(parts -> {
                    FilePart filePart = (FilePart) parts.get("file").get(0);
                    String customTitle = ((FormFieldPart) parts.get("fileName").get(0)).value(); // Usar para el título
                    String fileExtension = filePart.filename().substring(filePart.filename().lastIndexOf("."));
                    
                    // Generar un nombre de archivo aleatorio
                    String randomFileName = UUID.randomUUID().toString() + fileExtension;
                    String fullPath = VIDEO_DIRECTORY + randomFileName;

                    File videoFile = new File(fullPath);
                    
                    return filePart.transferTo(videoFile)
                        .then(Mono.defer(() -> {
                            System.out.println("Creando entidad Video para guardar en MongoDB");
                            Video video = new Video();
                            video.setName(randomFileName);
                            video.setUrl(fullPath);
                            video.setTitle(customTitle);
                            video.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                            System.out.println("Guardando video en MongoDB...");
                            return videoRepository.save(video)
                                    .doOnSuccess(savedVideo -> System.out.println("Guardado correctamente: " + savedVideo.getId()))
                                    .flatMap(savedVideo -> ServerResponse.status(HttpStatus.CREATED)
                                            .bodyValue("Video subido y guardado en la base de datos: " + savedVideo));
                        }));
                })
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue("Error al subir el video: " + e.getMessage()));
    }

    public Mono<ServerResponse> getVideoByName(ServerRequest request) {
        String videoName = request.pathVariable("videoName");

        return videoRepository.findByName(videoName)
                .flatMap(video -> {
                    File videoFile = new File(video.getUrl());
                    if (!videoFile.exists()) {
                        return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue("El video no existe en el sistema de archivos.");
                    }
                    Resource resource = new FileSystemResource(videoFile);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .bodyValue(resource);
                })
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue("No se encontró el video en la base de datos: " + videoName))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue("Error al recuperar el video: " + e.getMessage()));
    }

    public Mono<ServerResponse> getAllVideos(ServerRequest request) {
        return videoRepository.findAll()
                .collectList()
                .flatMap(videos -> ServerResponse.ok()
                        .bodyValue(videos))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue("Error al recuperar los videos: " + e.getMessage()));
    }

    public Mono<ServerResponse> addVideoToPost(ServerRequest request) {
        String postId = request.pathVariable("postId");
        String videoName = request.pathVariable("videoName");
        
        // Validar los parámetros
        if (postId == null || videoName == null) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .bodyValue("Se requieren los parámetros postId y videoName.");
        }
    
        // Buscar el video en la base de datos
        return videoRepository.findByName(videoName)
                .flatMap(video -> postRepository.findById(postId)
                        .flatMap(post -> {

                            // Inicializar la lista si no existe
                            if (post.getVideos() == null) {
                                post.setVideos(new ArrayList<>());
                            }

                            // Agregar el objeto Video al Post
                            post.getVideos().add(video);
                            return postRepository.save(post)
                                    .flatMap(savedPost -> ServerResponse.status(HttpStatus.OK)
                                            .bodyValue("El video '" + videoName + "' ha sido añadido al post con ID: " + postId));
                        })
                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                .bodyValue("No se encontró el post con ID: " + postId)))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue("No se encontró el video con nombre: " + videoName))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue("Error al añadir el video al post: " + e.getMessage()));
    }
    
}
