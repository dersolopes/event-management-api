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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

    @Transactional
    public void inscreverParticipante(UUID eventId, UUID usuarioId) {
        // 1. Valida se o usuário existe no sistema
        User participante = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário participante não encontrado"));

        // 2. ORDEM SÊNIOR: Tenta atualizar o contador atomicamente direto no banco primeiro
        int linhasAfetadas = eventRepository.reservarVaga(eventId);

        // 3. Se retornar 0, significa que o evento já atingiu a capacidade máxima no banco
        if (linhasAfetadas == 0) {
            throw new BusinessException("Desculpe, o evento já atingiu a capacidade máxima de participantes!");
        }

        // 4. Se chegou aqui, a vaga está garantida de forma segura. Buscamos o evento para vincular na inscrição.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        // 5. Salva o registro da inscrição na tabela REGISTRATIONS
        Registration inscricao = Registration.builder()
                .user(participante)
                .event(event)
                .status(RegistrationStatus.CONFIRMED)
                .build();

        registrationRepository.save(inscricao);
    }
}
