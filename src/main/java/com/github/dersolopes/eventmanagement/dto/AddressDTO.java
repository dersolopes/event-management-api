package com.github.dersolopes.eventmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {

    @NotBlank(message = "A rua é obrigatória")
    private String street;
    @NotBlank(message = "A cidade é obrigatória")
    private String city;
    @NotBlank(message = "O Estado é obrigatório")
    @Size(min = 2, max = 2, message = "O estado deve conter exatamente 2 caracteres (ex: SP)")
    private String state;

}
