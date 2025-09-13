package com.trackhub.trackhub_project.dto;

import java.time.LocalDateTime;

public record AudioFileResponse(
        Long id,
        String fileName,
        String mimeType,
        Long fileSize,
        LocalDateTime uploadedAt
) {}