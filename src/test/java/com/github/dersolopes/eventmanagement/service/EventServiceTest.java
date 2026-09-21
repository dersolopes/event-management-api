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
import com.github.dersolopes.eventmanagement.service.impl.EventServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService - Testes Unitários de Regra de Negócio")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;
    // CORRECAO: EventServiceImpl é a classe concreta, o @InjectMocks daria erro:
        // Cannot instantiate @InjectMocks field named 'eventService'!
        // Cause: the type 'EventService' is an interface.

    @Nested
    @DisplayName("Método: criarEvento()")
    class CriarEventoContext {

        @Nested
        @DisplayName("Quando os dados de entrada são válidos")
        class QuandoDadosValidos {

            @Test
            @DisplayName("Deve salvar o evento com sucesso, definir status ACTIVE e vincular a categoria")
            void deveCriarEventoComSucesso() {
                // Given (Arrange)
                Long categoryId = 1L;
                EventRequestDTO requestDTO = new EventRequestDTO();
                requestDTO.setTitle("Dev Conference 2026");
                requestDTO.setCategoryId(categoryId);

                Category category = Category.builder()
                        .id(categoryId)
                        .name("Tecnologia")
                        .build();

                Event eventMapeado = Event.builder()
                        .title("Dev Conference 2026")
                        .build();

                Event eventSalvo = Event.builder()
                        .id(UUID.randomUUID())
                        .title("Dev Conference 2026")
                        .category(category)
                        .status(EventStatus.ACTIVE)
                        .build();

                EventResponseDTO responseEsperado = new EventResponseDTO();

                when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
                when(eventMapper.toEntity(requestDTO)).thenReturn(eventMapeado);
                when(eventRepository.save(any(Event.class))).thenReturn(eventSalvo);
                when(eventMapper.toResponseDTO(eventSalvo)).thenReturn(responseEsperado);

                // When (Act)
                EventResponseDTO result = eventService.criarEvento(requestDTO);

                // Then (Assert)
                assertThat(result).isNotNull().isEqualTo(responseEsperado);
                assertThat(eventMapeado.getStatus()).isEqualTo(EventStatus.ACTIVE);
                assertThat(eventMapeado.getCategory()).isEqualTo(category);

                verify(categoryRepository, times(1)).findById(categoryId);
                verify(eventRepository, times(1)).save(eventMapeado);
                verify(eventMapper, times(1)).toResponseDTO(eventSalvo);
            }
        }

        @Nested
        @DisplayName("Quando a categoria informada não existe")
        class QuandoCategoriaNaoExiste {

            @Test
            @DisplayName("Deve lançar ResourceNotFoundException e não interagir com a persistência de eventos")
            void deveLancarExcecaoQuandoCategoriaInexistente() {
                // Given (Arrange)
                Long categoryIdInexistente = 99L;
                EventRequestDTO requestDTO = new EventRequestDTO();
                requestDTO.setCategoryId(categoryIdInexistente);

                when(categoryRepository.findById(categoryIdInexistente)).thenReturn(Optional.empty());

                // When (Act) & Then (Assert)
                assertThatThrownBy(() -> eventService.criarEvento(requestDTO))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Categoria não encontrada com o ID: 99");

                verify(eventRepository, never()).save(any());
                verify(eventMapper, never()).toEntity(any());
            }
        }
    }

    @Nested
    @DisplayName("Método: buscarPorId()")
    class BuscarPorIdContext {

        @Nested
        @DisplayName("Quando o evento existe no banco de dados")
        class QuandoEventoExiste {

            @Test
            @DisplayName("Deve retornar o DTO do evento com sucesso")
            void deveRetornarEventoQuandoIdExiste() {
                // Given (Arrange)
                UUID id = UUID.randomUUID();
                Event event = Event.builder().id(id).title("Meetup Java").build();
                EventResponseDTO responseDTO = new EventResponseDTO();

                when(eventRepository.findById(id)).thenReturn(Optional.of(event));
                when(eventMapper.toResponseDTO(event)).thenReturn(responseDTO);

                // When (Act)
                EventResponseDTO result = eventService.buscarPorId(id);

                // Then (Assert)
                assertThat(result).isNotNull().isEqualTo(responseDTO);

                verify(eventRepository, times(1)).findById(id);
                verify(eventMapper, times(1)).toResponseDTO(event);
            }
        }

        @Nested
        @DisplayName("Quando o evento não é encontrado")
        class QuandoEventoNaoExiste {

            @Test
            @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
            void deveLancarExcecaoQuandoIdInexistente() {
                // Given (Arrange)
                UUID idInexistente = UUID.randomUUID();
                when(eventRepository.findById(idInexistente)).thenReturn(Optional.empty());

                // When (Act) & Then (Assert)
                assertThatThrownBy(() -> eventService.buscarPorId(idInexistente))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Evento não encontrado");

                verify(eventRepository, times(1)).findById(idInexistente);
                verify(eventMapper, never()).toResponseDTO(any());
            }
        }
    }

    @Nested
    @DisplayName("Método: listarComFiltros()")
    class ListarComFiltrosContext {

        @Nested
        @DisplayName("Quando a consulta de filtro é executada com sucesso")
        class QuandoFiltrosAplicados {

            @Test
            @DisplayName("Deve buscar eventos filtrados e retornar a página mapeada para DTOs")
            @SuppressWarnings("unchecked")
            void deveRetornarPaginaDeEventosMapeados() {
                // Given (Arrange)
                String city = "Carapicuíba";
                Long categoryId = 1L;
                LocalDateTime startDate = LocalDateTime.now();
                Pageable pageable = PageRequest.of(0, 10);

                Event event = Event.builder().id(UUID.randomUUID()).title("Meetup Java").build();
                Page<Event> pageEventos = new PageImpl<>(List.of(event));
                EventResponseDTO responseDTO = new EventResponseDTO();

                when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageEventos);
                when(eventMapper.toResponseDTO(event)).thenReturn(responseDTO);

                // When (Act)
                Page<EventResponseDTO> result = eventService.listarComFiltros(city, categoryId, startDate, pageable);

                // Then (Assert)
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1).containsExactly(responseDTO);

                verify(eventRepository, times(1)).findAll(any(Specification.class), eq(pageable));
                verify(eventMapper, times(1)).toResponseDTO(event);
            }
        }
    }

    @Nested
    @DisplayName("Método: cancelarEvento()")
    class CancelarEventoContext {

        @Nested
        @DisplayName("Quando o evento existe e está ativo")
        class QuandoEventoValido {

            @Test
            @DisplayName("Deve atualizar o status do evento para CANCELED")
            void deveCancelarEventoComSucesso() {
                // Given (Arrange)
                UUID id = UUID.randomUUID();
                Event event = Event.builder().id(id).status(EventStatus.ACTIVE).build();

                when(eventRepository.findById(id)).thenReturn(Optional.of(event));
                when(eventRepository.save(event)).thenReturn(event);

                // When (Act)
                eventService.cancelarEvento(id);

                // Then (Assert)
                assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELED);

                verify(eventRepository, times(1)).findById(id);
                verify(eventRepository, times(1)).save(event);
            }
        }

        @Nested
        @DisplayName("Quando o evento a ser cancelado não é localizado")
        class QuandoEventoNaoExiste {

            @Test
            @DisplayName("Deve lançar ResourceNotFoundException e não realizar chamada ao repositório para salvar")
            void deveLancarExcecaoQuandoCancelarInexistente() {
                // Given (Arrange)
                UUID idInexistente = UUID.randomUUID();
                when(eventRepository.findById(idInexistente)).thenReturn(Optional.empty());

                // When (Act) & Then (Assert)
                assertThatThrownBy(() -> eventService.cancelarEvento(idInexistente))
                        .isInstanceOf(ResourceNotFoundException.class);

                verify(eventRepository, times(1)).findById(idInexistente);
                verify(eventRepository, never()).save(any());
            }
        }
    }
}