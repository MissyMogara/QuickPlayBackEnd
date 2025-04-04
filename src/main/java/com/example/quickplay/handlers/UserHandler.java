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
import com.example.quickplay.repositories.ProfileRepository;
import com.example.quickplay.security.JwtUtil;
import com.example.quickplay.services.UserService;

import reactor.core.publisher.Mono;

@Component
public class UserHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileRepository profileRepository;

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
                }
                );
    }

}

record AuthResponse(String token) {

}
