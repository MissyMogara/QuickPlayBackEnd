package com.example.quickplay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quickplay.models.Users;
import com.example.quickplay.repositories.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MyReactiveUserDetailsService userDetailsService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;
    
    public Mono<Users> register(Users user) {
        return Mono.just(user)
            .flatMap(newUser -> {
                // Validate user data
                if (newUser.getUsername() == null || newUser.getPassword() == null) {
                    return Mono.error(new IllegalArgumentException("Username and password are required"));
                }
                
                // Check if username already exists
                return userRepository.findByEmail(newUser.getEmail())
                    .flatMap(existingUser -> Mono.error(new IllegalArgumentException("Email already exists")))
                    .switchIfEmpty(Mono.defer(() -> {
                        // Establish date
                        newUser.setCreatedAt(String.valueOf(System.currentTimeMillis()));
                        // Hash password before saving
                        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
                        // Set default role if not provided
                        if (newUser.getRole() == null) {
                            newUser.setRole("USER");
                        }
                        return userRepository.save(newUser);
                    }))
                    .thenReturn(newUser);
            });
    }

    public Mono<String> verify(Users user) {
        return userRepository.findByUsername(user.getUsername())
            .flatMap(existingUser -> {
                if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                    return Mono.just("User verified successfully");
                }
                return Mono.error(new BadCredentialsException("Invalid credentials"));
            })
            .switchIfEmpty(Mono.error(new BadCredentialsException("User not found")));
    }
    
    public Mono<String> login(String username, String password) {
        return userDetailsService.findByUsername(username)
            .filter(userDetails -> passwordEncoder.matches(password, userDetails.getPassword()))
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")))
            .flatMap(userDetails -> Mono.just(jwtService.generateToken(username)));
    }

    
}