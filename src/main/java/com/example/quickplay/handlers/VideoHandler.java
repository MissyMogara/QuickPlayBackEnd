package com.example.quickplay.handlers;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class VideoHandler {

    private static final String VIDEO_DIRECTORY = "src/main/resources/videos/";

    public Mono<ServerResponse> uploadVideo(ServerRequest request) {
        return request.multipartData()
                .flatMap(parts -> {
                    FilePart filePart = (FilePart) parts.get("file").get(0);
                    String filePath = VIDEO_DIRECTORY + filePart.filename();
                    File videoFile = new File(filePath);

                    return filePart.transferTo(videoFile)
                            .then(ServerResponse.status(HttpStatus.CREATED)
                                    .bodyValue("Video subido exitosamente: " + filePath));
                })
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue("Error al subir el video: " + e.getMessage()));
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
