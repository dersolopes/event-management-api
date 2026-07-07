package com.github.dersolopes.eventmanagement.repository;

import com.github.dersolopes.eventmanagement.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /**
     * Tenta incrementar o contador de participantes atomicamente a nível de banco de dados,
     * garantindo que a operação só aconteça se ainda houver vagas disponíveis.
     *
     * @param eventId ID do evento alvo da inscrição
     * @return 1 se a vaga foi reservada com sucesso, ou 0 se o evento já estiver lotado.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Event e SET e.currentCount = e.currentCount + 1 " +
            "WHERE e.id = :eventId AND e.currentCount < e.capacity")
    int reservarVaga(@Param("eventId") UUID eventId);
}
