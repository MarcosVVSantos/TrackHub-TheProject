package com.trackhub.trackhub_project.dto;

import com.trackhub.trackhub_project.entity.enums.ProjectStatus;
import lombok.Builder;

@Builder
public record ProjectRequest(
        String nome,
        String descricao,
        ProjectStatus status
) {}