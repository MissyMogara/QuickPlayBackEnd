package com.example.quickplay.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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

    private List<String> transcodificarYObtenerSegmentos(String rutaOriginal, String rutaDestino) {
        try {
            // Ejecutar el proceso de transcodificación
            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\dev\\ffmpeg\\bin\\ffmpeg",
                    "-i", rutaOriginal,
                    "-map", "0",
                    "-codec:v", "libx264",
                    "-codec:a", "aac",
                    "-preset", "fast",
                    "-crf", "23",
                    "-profile:v", "baseline", // Perfil de compatibilidad
                    "-level", "3.0",          // Nivel adecuado
                    "-b:v", "2000k",
                    "-maxrate", "2000k",
                    "-bufsize", "2000k",
                    "-s", "1280x720",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-f", "hls",
                    rutaDestino
            );
            pb.inheritIO(); // Mostrar los logs en la consola
            Process process = pb.start();
            process.waitFor();
            System.out.println("Transcodificación completada: " + rutaDestino);
    
            // Leer el archivo .m3u8 para capturar los nombres de los segmentos .ts
            File m3u8File = new File(rutaDestino);
            List<String> segmentNames = new ArrayList<>();
    
            try (Scanner scanner = new Scanner(m3u8File)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.endsWith(".ts")) { // Capturar líneas con nombres de segmentos
                        segmentNames.add(line.trim());
                    }
                }
            }
    
            System.out.println("Segmentos encontrados: " + segmentNames);
            return segmentNames;
        } catch (Exception e) {
            System.err.println("Error al transcodificar el video: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    
    public Mono<ServerResponse> uploadVideo(ServerRequest request) {
        return request.multipartData()
                .flatMap(parts -> {
                        FilePart filePart = (FilePart) parts.get("file").get(0);
                        String customTitle = ((FormFieldPart) parts.get("fileName").get(0)).value(); // Título personalizado
                        String fileExtension = filePart.filename().substring(filePart.filename().lastIndexOf("."));

                        // Generar un nombre de archivo aleatorio
                        String randomName = UUID.randomUUID().toString();
                        String randomFileName = randomName + fileExtension;
                        String fullPath = VIDEO_DIRECTORY + randomFileName;

                        File videoFile = new File(fullPath);

                        return filePart.transferTo(videoFile)
                                .then(Mono.defer(() -> {
                                System.out.println("Creando entidad Video para guardar en MongoDB");
                                Video video = new Video();
                                video.setName(randomName + ".m3u8");

                                // Actualiza para que use la ruta del archivo HLS (.m3u8)
                                String hlsFilePath = VIDEO_DIRECTORY + randomFileName.replace(fileExtension, ".m3u8");
                                video.setUrl(hlsFilePath); // Ruta del archivo HLS

                                video.setTitle(customTitle);
                                video.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                                // Llamar a la función para transcodificar el video y capturar los segmentos .ts
                                List<String> segmentNames = transcodificarYObtenerSegmentos(fullPath, hlsFilePath);
                                video.setSegments(segmentNames); // Guardar los nombres de los segmentos .ts

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
                
                System.out.println("Solicitud recibida para el archivo: " + videoName);
            
                // Construir la ruta completa del archivo dentro del directorio configurado
                File videoFile = new File(VIDEO_DIRECTORY + videoName);
            
                if (videoFile.exists()) {
                    // Determina dinámicamente el tipo de contenido
                    String contentType = videoName.endsWith(".m3u8") 
                        ? "application/vnd.apple.mpegurl" 
                        : "video/mp2t";
            
                    System.out.println("Ruta completa del archivo: " + videoFile.getAbsolutePath());
                    System.out.println("Tipo de contenido: " + contentType);
            
                    return ServerResponse.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .bodyValue(new FileSystemResource(videoFile));
                }
            
                // Registrar si el archivo solicitado no existe
                System.out.println("Archivo no encontrado: " + videoName);
                return ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue("El archivo solicitado no existe: " + videoName);
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



// public Mono<ServerResponse> getVideoByName(ServerRequest request) {
//         String videoName = request.pathVariable("videoName");

//         return videoRepository.findByName(videoName)
//                 .flatMap(video -> {
//                     File videoFile = new File(video.getUrl());
//                     if (!videoFile.exists()) {
//                         return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue("El video no existe en el sistema de archivos.");
//                     }
//                     Resource resource = new FileSystemResource(videoFile);
//                     return ServerResponse.ok()
//                             .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                             .bodyValue(resource);
//                 })
//                 .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
//                         .bodyValue("No se encontró el video en la base de datos: " + videoName))
//                 .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                         .bodyValue("Error al recuperar el video: " + e.getMessage()));
//     }