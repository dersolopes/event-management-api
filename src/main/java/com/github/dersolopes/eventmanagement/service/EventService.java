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
import com.github.dersolopes.eventmanagement.repository.EventSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Transactional(readOnly = true)
    public Page<EventResponseDTO> listarComFiltros(String city, Long categoryId, LocalDateTime startDate, Pageable pageable) {

        // Usamos o metodo Specification.allOf(...)
        // Ele combina todas as regras em uma lista limpa.
        // Se um parâmetro vier nulo do body, a especificação dele retorna unrestricted()
        // e o Spring simplesmente ignora aquela cláusula na hora de montar o WHERE no banco.
        Specification<Event> spec = Specification.allOf(
                EventSpecification.byCity(city),
                EventSpecification.byCategory(categoryId),
                EventSpecification.byStartDateFrom(startDate)
        );

        // Busca no banco aplicando filtros e paginação
        Page<Event> eventosPage = eventRepository.findAll(spec, pageable);

        // Converte para DTO
        return eventosPage.map(eventMapper::toResponseDTO);
    }


}
