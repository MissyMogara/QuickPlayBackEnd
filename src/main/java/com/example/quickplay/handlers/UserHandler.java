package com.example.quickplay.handlers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.dto.UserLoginDTO;
import com.example.quickplay.dto.UserRegisterDTO;
import com.example.quickplay.entities.Profile;
import com.example.quickplay.entities.User;
import com.example.quickplay.repositories.ProfileRepository;
import com.example.quickplay.repositories.UserRepository;
import com.example.quickplay.security.JwtUtil;
import com.example.quickplay.services.UserService;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

import reactor.core.publisher.Mono;

@Component
public class UserHandler {

        private final ReactiveMongoTemplate reactiveMongoTemplate;

        @Autowired
        private UserService userService;

        @Autowired
        private UserRepository userRepository;
        
        @Autowired
        private ProfileRepository profileRepository;

        public UserHandler(ReactiveMongoTemplate reactiveMongoTemplate) {
                this.reactiveMongoTemplate = reactiveMongoTemplate;
        }

        @Autowired
        private JwtUtil jwtUtil;

        public Mono<ServerResponse> register(ServerRequest request) {
                return request.bodyToMono(UserRegisterDTO.class)
                        .flatMap(dto ->
                                // Verificamos si ya existe un usuario con ese nombre
                                userService.findByUsername(dto.username())
                                        .flatMap(existingUser ->
                                                ServerResponse.badRequest()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue("El usuario ya existe")
                                        )
                                        .switchIfEmpty(
                                                // Verificamos si ya existe un usuario con el mismo email
                                                userService.findByEmail(dto.email())
                                                        .flatMap(existingEmailUser ->
                                                                ServerResponse.badRequest()
                                                                        .contentType(MediaType.APPLICATION_JSON)
                                                                        .bodyValue("El correo ya está en uso")
                                                        )
                                                        .switchIfEmpty(
                                                                // Si no hay duplicados, guardamos el usuario
                                                                userService.save(dto)
                                                                        .flatMap(savedUser -> {
                                                                        // Creamos el perfil con el userId
                                                                        Profile profile = new Profile();
                                                                        profile.setUserId(savedUser.getId());
                                                                        profile.setCreatedAt(LocalDateTime.now().toString());
                                                                        
                                                                        return profileRepository.save(profile)
                                                                                .then(ServerResponse.ok()
                                                                                        .contentType(MediaType.APPLICATION_JSON)
                                                                                        .bodyValue(savedUser));
                                                                        })
                                                        )
                                        )       
                );
        }

        public Mono<ServerResponse> login(ServerRequest request) {

        return request.bodyToMono(UserLoginDTO.class)
                .flatMap(dto -> {
                    // Busca al usuario por username
                    return userService.findByUsername(dto.username())
                            // Verifica que la contraseña proporcionada coincida con la almacenada
                            .filter(user -> userService.passwordMatches(dto.password(), user.getPassword()))
                            .flatMap(user -> {
                                // Si coincide, genera un token y lo retorna
                                String token = jwtUtil.generateToken(user);

                                // Retorna la respuesta con el usuario
                                return ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(new AuthResponse(token));
                            })
                            // Si no se encontró el usuario o la contraseña no coincide, retorna UNAUTHORIZED
                            .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
                });
        }

        public Mono<ServerResponse> getUserById(ServerRequest request) {
                String userId = request.pathVariable("userId");
                return userRepository.findById(userId)
                        .flatMap(user -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(user))
                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("User not found"))
                        .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("Error retrieving user: " + e.getMessage()));
        }

        public Mono<ServerResponse> updateUser(ServerRequest request) {
                String userId = request.pathVariable("userId");
                return request.bodyToMono(User.class)
                        .flatMap(dto ->
                                userRepository.findById(userId)
                                        .flatMap(existingUser -> {
                                                // Actualiza el usuario con los nuevos datos
                                                if (dto.getUsername() != null && !dto.getUsername().equals(existingUser.getUsername())) {
                                                        existingUser.setUsername(dto.getUsername());
                                                    }
                                                    if (dto.getEmail() != null && !dto.getEmail().equals(existingUser.getEmail())) {
                                                        existingUser.setEmail(dto.getEmail());
                                                    }
                                                    if (dto.getName() != null && !dto.getName().equals(existingUser.getName())) {
                                                        existingUser.setName(dto.getName());
                                                    }
                                                    if (dto.getSurname() != null && !dto.getSurname().equals(existingUser.getSurname())) {
                                                        existingUser.setSurname(dto.getSurname());
                                                    }
                                                    if (dto.getRole() != null && !dto.getRole().equals(existingUser.getRole())) {
                                                        existingUser.setRole(dto.getRole());
                                                    }
                                                return userRepository.save(existingUser)
                                                        .flatMap(updatedUser -> ServerResponse.ok()
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .bodyValue(updatedUser));
                                        })
                                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue("User not found"))
                        )
                        .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("Error updating user: " + e.getMessage()));
        }

        public Mono<ServerResponse> deleteUser(ServerRequest request) {
                String userId = request.pathVariable("userId");
            
                return userRepository.findById(userId)
                        .flatMap(existingUser -> 
                            profileRepository.deleteByUserId(userId) // Elimina el perfil
                                    .then(userRepository.delete(existingUser)) // Luego elimina el usuario
                                    .then(ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue("User and profile deleted"))
                        )
                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("User not found"))
                        .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("Error deleting user: " + e.getMessage()));
        }

        public Mono<ServerResponse> followUser(ServerRequest request) {
                String userId = request.pathVariable("userId");
                String followerId = request.pathVariable("followerId");

                return reactiveMongoTemplate.update(Profile.class)
                        .matching(Criteria.where("userId").is(userId))
                        .apply(new Update().addToSet("followers", followerId))
                        .findAndModify()
                        .flatMap(updatedProfile -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(updatedProfile))
                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("User not found"))
                        .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("Error following user: " + e.getMessage()));
        }

}

record AuthResponse(String token) {

}
