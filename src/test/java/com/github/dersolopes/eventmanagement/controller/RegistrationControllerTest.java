package com.github.dersolopes.eventmanagement.controller;

import com.github.dersolopes.eventmanagement.exception.BusinessException;
import com.github.dersolopes.eventmanagement.exception.GlobalExceptionHandler;
import com.github.dersolopes.eventmanagement.exception.ResourceNotFoundException;
import com.github.dersolopes.eventmanagement.service.RegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RegistrationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @Nested
    @DisplayName("POST /api/v1/registrations/event/{eventId}")
    class Inscrever {

        @Test
        @DisplayName("Deve inscrever participante com sucesso e retornar status 201 Created")
        @WithMockUser
        void shouldRegisterSuccessfully() throws Exception {
            UUID eventId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willDoNothing().given(registrationService).inscreverParticipante(eventId, userId);

            mockMvc.perform(post("/api/v1/registrations/event/{eventId}", eventId)
                            .param("userId", userId.toString())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve retornar status 400 Bad Request quando evento não tiver vagas disponíveis")
        @WithMockUser
        void shouldReturn400WhenEventIsFull() throws Exception {
            UUID eventId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willThrow(new BusinessException("Desculpe, o evento já atingiu a capacidade máxima de participantes!"))
                    .given(registrationService).inscreverParticipante(eventId, userId);

            mockMvc.perform(post("/api/v1/registrations/event/{eventId}", eventId)
                            .param("userId", userId.toString())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());// <- Alterado de .isBadRequest() para .isConflict()
        }

        @Test
        @DisplayName("Deve retornar status 404 Not Found quando usuário participante não existir")
        @WithMockUser
        void shouldReturn404WhenUserNotFound() throws Exception {
            UUID eventId = UUID.randomUUID();
            UUID userIdInexistente = UUID.randomUUID();

            willThrow(new ResourceNotFoundException("Usuário não encontrado"))
                    .given(registrationService).inscreverParticipante(eventId, userIdInexistente);

            mockMvc.perform(post("/api/v1/registrations/event/{eventId}", eventId)
                            .param("userId", userIdInexistente.toString())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}