package com.example.quickplay.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.quickplay.models.Users;
import com.example.quickplay.services.UserService;
import com.example.quickplay.models.LoginRequest;

import reactor.core.publisher.Mono;

@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody Users user) {
    return userService.register(user)
        .map(savedUser -> ResponseEntity.ok("User registered successfully"))
        .onErrorResume(e -> Mono.just(
            ResponseEntity.badRequest().body(e.getMessage())
        )); 
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody LoginRequest request) {
        return userService.login(request.getUsername(), request.getPassword())
            .map(token -> ResponseEntity.ok().body(token))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage())
            ));
    }
}
