package com.example.quickplay.services;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.quickplay.models.UserPrincipal;
import com.example.quickplay.repositories.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class MyReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public MyReactiveUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        System.out.println("Searching for user in database: " + username);
        return userRepository.findByUsername(username)
            .doOnNext(user -> System.out.println("User found in repository: " + user.getUsername()))
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found in database")))
            .map(UserPrincipal::new);
    }

}