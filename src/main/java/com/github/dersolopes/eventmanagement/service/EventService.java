package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.dto.EventRequestDTO;
import com.github.dersolopes.eventmanagement.dto.EventResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EventService {

    EventResponseDTO criarEvento(EventRequestDTO dto);

    EventResponseDTO buscarPorId(UUID id);

    Page<EventResponseDTO> listarComFiltros(String city, Long categoryId, LocalDateTime startDate, Pageable pageable);

    EventResponseDTO atualizarEvento(UUID id, EventRequestDTO dto);

    void cancelarEvento(UUID id);
}