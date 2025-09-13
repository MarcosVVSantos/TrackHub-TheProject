package com.trackhub.trackhub_project.dto;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String email,
        String nome,
        String role
) {}