package com.example.quickplay.services;

import com.example.quickplay.dto.UserRegisterDTO;
import com.example.quickplay.entities.User;
import com.example.quickplay.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public Mono<User> findByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    public Mono<User> save(UserRegisterDTO userRegisterDTO) {
        User user = new User();
        user.setEmail(userRegisterDTO.email());
        user.setUsername(userRegisterDTO.username());
        user.setRole(userRegisterDTO.role());
        user.setName(userRegisterDTO.name());
        user.setSurname(userRegisterDTO.surname());
        user.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        // Encripta la contraseña antes de guardarla
        user.setPassword(passwordEncoder.encode(userRegisterDTO.password()));

        return userRepository.save(user);

    }

    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}