package com.example.quickplay.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;

import com.example.quickplay.repositories.ProfileRepository;

@Component
public class ProfileHandler {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    private final ProfileRepository profileRepository;

    public ProfileHandler(ProfileRepository profileRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.profileRepository = profileRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    

}
