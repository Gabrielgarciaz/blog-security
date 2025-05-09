package com.gabrielgarcia.blogsecurity.entities.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
