package com.github.dersolopes.eventmanagement.dto;

import com.github.dersolopes.eventmanagement.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// DTO de Cadastro de Usuário
public record RegisterRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ']{2,}(\\s+[A-Za-zÀ-ÿ']{2,})+$", message = "Informar nome e sobrenome com pelo menos 2 letras cada")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        Role role // Ex: USER ou ADMIN (pode ter default USER na service)
) {}