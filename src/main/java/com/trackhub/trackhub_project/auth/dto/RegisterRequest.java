package com.trackhub.trackhub_project.auth.dto;

import com.trackhub.trackhub_project.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private String cpf;
    private Role role;
}