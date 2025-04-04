package com.example.quickplay.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.entities.Profile;
import com.example.quickplay.repositories.ProfileRepository;

import reactor.core.publisher.Mono;

@Component
public class ProfileHandler {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    private final ProfileRepository profileRepository;

    public ProfileHandler(ProfileRepository profileRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.profileRepository = profileRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<ServerResponse> getProfileByUserId(ServerRequest request) {
        String userId = request.pathVariable("userId");

        return profileRepository.findByUserId(userId)
                .flatMap(profile -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(profile))
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Profile not found"));
    }

}
