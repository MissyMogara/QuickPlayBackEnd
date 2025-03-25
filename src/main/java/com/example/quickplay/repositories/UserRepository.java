package com.example.quickplay.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.quickplay.models.Users;

@Repository
public interface UserRepository extends MongoRepository<Users, String> {    Users findByUsername(String username);
}
