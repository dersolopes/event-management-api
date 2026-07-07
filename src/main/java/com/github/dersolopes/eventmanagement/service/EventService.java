package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.dto.EventRequestDTO;
import com.github.dersolopes.eventmanagement.dto.EventResponseDTO;
import com.github.dersolopes.eventmanagement.entity.Category;
import com.github.dersolopes.eventmanagement.entity.Event;
import com.github.dersolopes.eventmanagement.enums.EventStatus;
import com.github.dersolopes.eventmanagement.exception.ResourceNotFoundException;
import com.github.dersolopes.eventmanagement.mapper.EventMapper;
import com.github.dersolopes.eventmanagement.repository.CategoryRepository;
import com.github.dersolopes.eventmanagement.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventResponseDTO criarEvento (EventRequestDTO dto){

// 1. Busca a categoria no banco para garantir que ela existe
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + dto.getCategoryId()));

        // 2. Usa o MapStruct para converter o DTO básico na Entidade JPA
        Event event = eventMapper.toEntity(dto);

        // 3. Aplica as regras de negócio iniciais de infraestrutura
        event.setCategory(category);
        event.setStatus(EventStatus.ACTIVE); // Todo evento nasce ATIVO

        // 4. Salva no banco de dados PostgreSQL
        Event eventSalvo = eventRepository.save(event);

        // 5. Devolve o DTO de resposta limpo para o usuário
        return eventMapper.toResponseDTO(eventSalvo);
    }

}
