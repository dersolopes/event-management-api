package com.github.dersolopes.eventmanagement.service.impl;

import com.github.dersolopes.eventmanagement.dto.EventRequestDTO;
import com.github.dersolopes.eventmanagement.dto.EventResponseDTO;
import com.github.dersolopes.eventmanagement.entity.Category;
import com.github.dersolopes.eventmanagement.entity.Event;
import com.github.dersolopes.eventmanagement.enums.EventStatus;
import com.github.dersolopes.eventmanagement.exception.BusinessException;
import com.github.dersolopes.eventmanagement.exception.ResourceNotFoundException;
import com.github.dersolopes.eventmanagement.mapper.EventMapper;
import com.github.dersolopes.eventmanagement.repository.CategoryRepository;
import com.github.dersolopes.eventmanagement.repository.EventRepository;
import com.github.dersolopes.eventmanagement.repository.EventSpecification;
import com.github.dersolopes.eventmanagement.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponseDTO criarEvento(EventRequestDTO dto) {
        log.info("Iniciando criação de evento com o título: '{}'", dto.getTitle());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Falha na criação de evento: Categoria ID {} não encontrada", dto.getCategoryId());
                    return new ResourceNotFoundException("Categoria não encontrada com o ID: " + dto.getCategoryId());
                });

        Event event = eventMapper.toEntity(dto);
        event.setCategory(category);
        event.setStatus(EventStatus.ACTIVE);

        Event eventSalvo = eventRepository.save(event);
        log.info("Evento criado com sucesso. ID: {}", eventSalvo.getId());

        return eventMapper.toResponseDTO(eventSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO buscarPorId(UUID id) {
        log.info("Buscando evento pelo ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Evento não encontrado para o ID: {}", id);
                    return new ResourceNotFoundException("Evento não encontrado com o ID: " + id);
                });

        return eventMapper.toResponseDTO(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> listarComFiltros(String city, Long categoryId, LocalDateTime startDate, Pageable pageable) {
        log.info("Listando eventos com filtros - Cidade: '{}', Categoria ID: {}, Data Inicial: {}", city, categoryId, startDate);

        Specification<Event> spec = Specification.allOf(
                EventSpecification.byCity(city),
                EventSpecification.byCategory(categoryId),
                EventSpecification.byStartDateFrom(startDate)
        );

        Page<Event> eventosPage = eventRepository.findAll(spec, pageable);
        return eventosPage.map(eventMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public EventResponseDTO atualizarEvento(UUID id, EventRequestDTO dto) {
        log.info("Iniciando atualização do evento ID: {}", id);

        Event eventExistente = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha na atualização: Evento ID {} não encontrado", id);
                    return new ResourceNotFoundException("Evento não encontrado com o ID: " + id);
                });

        if (eventExistente.getStatus() == EventStatus.CANCELED) {
            log.warn("Falha na atualização: Evento ID {} está CANCELADO", id);
            throw new BusinessException("Eventos cancelados não podem ser atualizados");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Falha na atualização de evento: Categoria ID {} não encontrada", dto.getCategoryId());
                    return new ResourceNotFoundException("Categoria não encontrada com o ID: " + dto.getCategoryId());
                });

        eventMapper.updateEntityFromDto(dto, eventExistente);
        eventExistente.setCategory(category);

        Event eventAtualizado = eventRepository.save(eventExistente);
        log.info("Evento ID {} atualizado com sucesso", id);

        return eventMapper.toResponseDTO(eventAtualizado);
    }

    @Override
    @Transactional
    public void cancelarEvento(UUID id) {
        log.info("Solicitada alteração de status para CANCELADO no evento ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao cancelar: Evento ID {} não encontrado", id);
                    return new ResourceNotFoundException("Evento não encontrado com o ID: " + id);
                });

        if (event.getStatus() == EventStatus.CANCELED) {
            log.warn("Evento ID {} já se encontra cancelado", id);
            throw new BusinessException("O evento já está cancelado");
        }

        event.setStatus(EventStatus.CANCELED);
        eventRepository.save(event);
        log.info("Evento ID {} cancelado com sucesso", id);
    }
}