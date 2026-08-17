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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    @Nested
    @DisplayName("Testes do método criarEvento()")
    class CriarEventoTestes {

        @Test
        @DisplayName("Deve criar evento com sucesso e definir status ACTIVE")
        void criarEvento_ComSucesso_QuandoDadosValidos() {
            // ARRANGE
            Long categoryId = 1L;
            EventRequestDTO dto = new EventRequestDTO();
            dto.setTitle("Dev Conference 2026");
            dto.setCategoryId(categoryId);

            Category category = Category.builder().id(categoryId).name("Tecnologia").build();
            Event eventIncompleto = Event.builder().title("Dev Conference 2026").build();
            Event eventSalvo = Event.builder().id(UUID.randomUUID()).title("Dev Conference 2026").category(category).status(EventStatus.ACTIVE).build();
            EventResponseDTO responseDTO = new EventResponseDTO();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(eventMapper.toEntity(dto)).thenReturn(eventIncompleto);
            when(eventRepository.save(any(Event.class))).thenReturn(eventSalvo);
            when(eventMapper.toResponseDTO(eventSalvo)).thenReturn(responseDTO);

            // ACT
            EventResponseDTO result = eventService.criarEvento(dto);

            // ASSERT
            assertNotNull(result);
            assertEquals(EventStatus.ACTIVE, eventIncompleto.getStatus());
            assertEquals(category, eventIncompleto.getCategory());

            verify(categoryRepository, times(1)).findById(categoryId);
            verify(eventRepository, times(1)).save(eventIncompleto);
            verify(eventMapper, times(1)).toResponseDTO(eventSalvo);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando a categoria informada não existir")
        void criarEvento_DeveLancarExcecao_QuandoCategoriaNaoExiste() {
            // ARRANGE
            Long categoryIdInexistente = 99L;
            EventRequestDTO dto = new EventRequestDTO();
            dto.setCategoryId(categoryIdInexistente);

            when(categoryRepository.findById(categoryIdInexistente)).thenReturn(Optional.empty());

            // ACT & ASSERT
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> eventService.criarEvento(dto)
            );

            assertTrue(exception.getMessage().contains("Categoria não encontrada com o ID: 99"));
            verify(eventRepository, never()).save(any());
            verify(eventMapper, never()).toEntity(any());
        }
    }

    @Nested
    @DisplayName("Testes do método listarComFiltros()")
    class ListarComFiltrosTestes {

        @Test
        @DisplayName("Deve buscar eventos com filtros e retornar página mapeada para DTO")
        @SuppressWarnings("unchecked")
        void listarComFiltros_DeveRetornarPaginaDeEventos() {
            // ARRANGE
            String city = "Carapicuíba";
            Long categoryId = 1L;
            LocalDateTime startDate = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, 10);

            Event event = Event.builder().id(UUID.randomUUID()).title("Meetup Java").build();
            Page<Event> pageEventos = new PageImpl<>(List.of(event));
            EventResponseDTO responseDTO = new EventResponseDTO();

            when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageEventos);
            when(eventMapper.toResponseDTO(event)).thenReturn(responseDTO);

            // ACT
            Page<EventResponseDTO> result = eventService.listarComFiltros(city, categoryId, startDate, pageable);

            // ASSERT
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(eventRepository, times(1)).findAll(any(Specification.class), eq(pageable));
            verify(eventMapper, times(1)).toResponseDTO(event);
        }
    }
}