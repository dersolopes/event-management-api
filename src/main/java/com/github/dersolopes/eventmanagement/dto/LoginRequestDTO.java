package com.github.dersolopes.eventmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Pattern;

public record LoginRequestDTO(
        @NotBlank(message= "O e-mail é obrigatório")
        @Email(message= "Formato do e-mail invalido")
        String email,
        @NotBlank(message= "A Senha é obrigatória")
        //@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "A Senha deve conter pelo menos 8 caracteres, 1 letra maiúscula, 1 letra minúscula, 1 número e 1 caractere especial")
        String password
) {
}