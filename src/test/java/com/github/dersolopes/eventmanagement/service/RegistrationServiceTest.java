package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.entity.Event;
import com.github.dersolopes.eventmanagement.entity.Registration;
import com.github.dersolopes.eventmanagement.entity.User;
import com.github.dersolopes.eventmanagement.exception.BusinessException;
import com.github.dersolopes.eventmanagement.exception.ResourceNotFoundException;
import com.github.dersolopes.eventmanagement.repository.EventRepository;
import com.github.dersolopes.eventmanagement.repository.RegistrationRepository;
import com.github.dersolopes.eventmanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @Nested
    @DisplayName("Testes do método inscreverParticipante()")
    class InscreverParticipanteTestes {

        @Test
        @DisplayName("Deve realizar a inscrição do participante com sucesso quando houver vagas disponíveis")
        void inscreverParticipante_ComSucesso_QuandoHouverVagas() {
            // ARRANGE
            UUID eventId = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();

            User user = User.builder().id(usuarioId).email("user@email.com").build();
            Event event = Event.builder().id(eventId).title("Tech Conference").build();

            when(userRepository.findById(usuarioId)).thenReturn(Optional.of(user));
            when(eventRepository.reservarVaga(eventId)).thenReturn(1); // 1 linha afetada = vaga reservada
            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

            // ACT
            assertDoesNotThrow(() -> registrationService.inscreverParticipante(eventId, usuarioId));

            // ASSERT
            verify(userRepository, times(1)).findById(usuarioId);
            verify(eventRepository, times(1)).reservarVaga(eventId);
            verify(eventRepository, times(1)).findById(eventId);
            verify(registrationRepository, times(1)).save(any(Registration.class));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando o usuário não for encontrado")
        void inscreverParticipante_DeveLancarExcecao_QuandoUsuarioNaoExiste() {
            // ARRANGE
            UUID eventId = UUID.randomUUID();
            UUID usuarioInexistenteId = UUID.randomUUID();

            when(userRepository.findById(usuarioInexistenteId)).thenReturn(Optional.empty());

            // ACT & ASSERT
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> registrationService.inscreverParticipante(eventId, usuarioInexistenteId)
            );

            assertEquals("Usuário participante não encontrado", exception.getMessage());
            verify(eventRepository, never()).reservarVaga(any());
            verify(registrationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando o evento não tiver mais vagas (reservarVaga retornar 0)")
        void inscreverParticipante_DeveLancarExcecao_QuandoEventoSemVagas() {
            // ARRANGE
            UUID eventId = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            User user = User.builder().id(usuarioId).build();

            when(userRepository.findById(usuarioId)).thenReturn(Optional.of(user));
            when(eventRepository.reservarVaga(eventId)).thenReturn(0); // 0 linhas afetadas = lotado

            // ACT & ASSERT
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> registrationService.inscreverParticipante(eventId, usuarioId)
            );

            assertTrue(exception.getMessage().contains("capacidade máxima"));
            verify(eventRepository, times(1)).reservarVaga(eventId);
            verify(eventRepository, never()).findById(any());
            verify(registrationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException se a vaga for reservada mas o evento for deletado em seguida")
        void inscreverParticipante_DeveLancarExcecao_QuandoEventoNaoEncontradoAposReserva() {
            // ARRANGE
            UUID eventId = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            User user = User.builder().id(usuarioId).build();

            when(userRepository.findById(usuarioId)).thenReturn(Optional.of(user));
            when(eventRepository.reservarVaga(eventId)).thenReturn(1);
            when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

            // ACT & ASSERT
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> registrationService.inscreverParticipante(eventId, usuarioId)
            );

            assertEquals("Evento não encontrado", exception.getMessage());
            verify(registrationRepository, never()).save(any());
        }
    }
}