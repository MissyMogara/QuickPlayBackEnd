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

    public Mono<ServerResponse> updateProfile(ServerRequest request) {
        String userId = request.pathVariable("userId");

        return request.bodyToMono(Profile.class)
                .flatMap(profile -> profileRepository.findByUserId(userId)
                        .flatMap(existingProfile -> {
                            if (profile.getBio() != null && !profile.getBio().equals(existingProfile.getBio())) {
                                existingProfile.setBio(profile.getBio());
                            }
                            if (profile.getProfilePictureUrl() != null && !profile.getProfilePictureUrl().equals(existingProfile.getProfilePictureUrl())) {
                                existingProfile.setProfilePictureUrl(profile.getProfilePictureUrl());
                            }
                            return profileRepository.save(existingProfile);
                        })
                        .flatMap(updatedProfile -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(updatedProfile))
                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("Profile not found")))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Error updating profile: " + e.getMessage()));
    }

}
