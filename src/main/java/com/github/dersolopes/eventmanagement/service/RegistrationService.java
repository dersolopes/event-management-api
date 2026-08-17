package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.entity.Event;
import com.github.dersolopes.eventmanagement.entity.Registration;
import com.github.dersolopes.eventmanagement.entity.User;
import com.github.dersolopes.eventmanagement.enums.RegistrationStatus;
import com.github.dersolopes.eventmanagement.exception.BusinessException;
import com.github.dersolopes.eventmanagement.exception.ResourceNotFoundException;
import com.github.dersolopes.eventmanagement.repository.EventRepository;
import com.github.dersolopes.eventmanagement.repository.RegistrationRepository;
import com.github.dersolopes.eventmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

    @Transactional
    public void inscreverParticipante(UUID eventId, UUID usuarioId) {
        log.info("Iniciando processo de inscrição. Evento ID: {}, Usuário ID: {}", eventId, usuarioId);

        // 1. Valida se o usuário existe no sistema
        User participante = userRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    log.warn("Falha na inscrição: Usuário ID {} não encontrado", usuarioId);
                    return new ResourceNotFoundException("Usuário participante não encontrado");
                });

        // 2. ORDEM SÊNIOR: Tenta atualizar o contador atomicamente direto no banco primeiro
        log.debug("Tentando reservar vaga de forma atômica para o evento ID: {}", eventId);
        int linhasAfetadas = eventRepository.reservarVaga(eventId);

        // 3. Se retornar 0, significa que o evento já atingiu a capacidade máxima no banco
        if (linhasAfetadas == 0) {
            log.warn("Falha na inscrição: Evento ID {} atingiu a capacidade máxima de participantes", eventId);
            throw new BusinessException("Desculpe, o evento já atingiu a capacidade máxima de participantes!");
        }

        // 4. Se chegou aqui, a vaga está garantida de forma segura. Buscamos o evento para vincular na inscrição.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Erro crítico: Vaga reservada, mas evento ID {} não foi encontrado ao buscar dados completos", eventId);
                    return new ResourceNotFoundException("Evento não encontrado");
                });

        // 5. Salva o registro da inscrição na tabela REGISTRATIONS
        Registration inscricao = Registration.builder()
                .user(participante)
                .event(event)
                .status(RegistrationStatus.CONFIRMED)
                .build();

        Registration salva = registrationRepository.save(inscricao);
        log.info("Inscrição ID {} realizada com sucesso. Usuário: '{}' (ID: {}), Evento: '{}' (ID: {})",
                salva.getId(), participante.getEmail(), usuarioId, event.getTitle(), eventId);
    }
}