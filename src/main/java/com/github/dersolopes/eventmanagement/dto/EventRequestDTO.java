package com.github.dersolopes.eventmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestDTO {

    @NotBlank(message = "O título do evento é obrigatório")
    @Size (max = 150, message = "O título deve ter no máximo 150 caracteres")
    private String title;

    private String description;

    @NotNull(message = "A data de início do evento é obrigatória")
    @Future(message = "A data de início deve ser no futuro")
    private LocalDateTime startDate;

    @NotNull(message = "A data de fim do evento é obrigatória")
    @Future(message = "A data de fim deve ser no futuro")
    private LocalDateTime endDate;

    @NotNull(message = "A capacidade máxima é obrigatória")
    @Min(value = 1, message = "A capacidade mínima deve ser de pelo menos 1 participante")
    private Integer capacity;

    @NotNull(message = "O ID da categoria é obrigatório")
    private Long categoryId;

    @NotNull(message = "O endereço do evento é obrigatório")
    private AddressDTO address;

}
