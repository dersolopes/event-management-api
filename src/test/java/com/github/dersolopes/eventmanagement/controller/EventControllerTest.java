package com.github.dersolopes.eventmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dersolopes.eventmanagement.dto.AddressDTO;
import com.github.dersolopes.eventmanagement.dto.EventRequestDTO;
import com.github.dersolopes.eventmanagement.dto.EventResponseDTO;
import com.github.dersolopes.eventmanagement.exception.GlobalExceptionHandler;
import com.github.dersolopes.eventmanagement.exception.ResourceNotFoundException;
import com.github.dersolopes.eventmanagement.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {EventController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    // Helper para criar um DTO válido de acordo com todas as anotações do @Valid
    private EventRequestDTO createValidRequestDTO() {
        EventRequestDTO request = new EventRequestDTO();
        request.setTitle("Tech Summit 2026");
        request.setDescription("Descrição do evento de tecnologia");
        request.setAddress(
                AddressDTO.builder()
                        .street("Rua dos Testes n 48")
                        .city("Sao Paulo").
                        state("Sao Paulo").build());
        request.setCategoryId(1L);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));
        request.setCapacity(100);
        return request;
    }

    @Nested
    @DisplayName("GET /api/v1/events")
    class Listar {

        @Test
        @DisplayName("Deve listar eventos paginados com sucesso e retornar status 200 OK")
        @WithMockUser
        void shouldReturnPagedEvents() throws Exception {
            EventResponseDTO responseDTO = new EventResponseDTO();
            responseDTO.setTitle("Tech Summit 2026");

            PageImpl<EventResponseDTO> page = new PageImpl<>(List.of(responseDTO));

            given(eventService.listarComFiltros(any(), any(), any(), any(Pageable.class)))
                    .willReturn(page);

            mockMvc.perform(get("/api/v1/events")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Tech Summit 2026"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/events")
    class Criar {

        @Test
        @DisplayName("Deve criar evento com sucesso e retornar status 201 Created")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateEventSuccessfully() throws Exception {
            EventRequestDTO request = createValidRequestDTO();

            EventResponseDTO response = new EventResponseDTO();
            response.setTitle("Tech Summit 2026");

            given(eventService.criarEvento(any(EventRequestDTO.class)))
                    .willReturn(response);

            mockMvc.perform(post("/api/v1/events")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Tech Summit 2026"));
        }

        @Test
        @DisplayName("Deve retornar status 404 Not Found quando a categoria informada não existir na Service")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            EventRequestDTO request = createValidRequestDTO();
            request.setCategoryId(99L); // ID inexistente, porém o DTO continua 100% válido para o @Valid

            given(eventService.criarEvento(any(EventRequestDTO.class)))
                    .willThrow(new ResourceNotFoundException("Categoria não encontrada com o ID: 99"));

            mockMvc.perform(post("/api/v1/events")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}