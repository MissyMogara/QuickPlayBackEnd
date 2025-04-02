package com.example.quickplay.dto;

public record UserRegisterDTO(String username, String email, String password, String role, String name, String surname) {
	// This is a record class for user registration details
}
