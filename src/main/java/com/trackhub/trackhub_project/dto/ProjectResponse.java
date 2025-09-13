package com.trackhub.trackhub_project.dto;

import com.trackhub.trackhub_project.entity.enums.ProjectStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProjectResponse(
        Long id,
        String nome,
        String descricao,
        ProjectStatus status,
        String criadorEmail, // Envie apenas o e-mail, não o objeto completo
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}