package com.github.dersolopes.eventmanagement.dto;

import com.github.dersolopes.eventmanagement.enums.EventStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer capacity;
    private Integer currentCount;
    private EventStatus status;
    private String categoryName; // Retorna direto o texto da categoria de forma limpa
    private AddressDTO address;
}
