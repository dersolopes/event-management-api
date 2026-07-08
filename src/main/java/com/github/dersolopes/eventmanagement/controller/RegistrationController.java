package com.github.dersolopes.eventmanagement.controller;

import com.github.dersolopes.eventmanagement.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/event/{eventId}")
    public ResponseEntity<Void> inscrever(
            @PathVariable UUID eventId,
            @RequestParam UUID userId) {

        registrationService.inscreverParticipante(eventId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
