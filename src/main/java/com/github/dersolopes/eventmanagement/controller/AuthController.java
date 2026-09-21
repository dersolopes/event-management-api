package com.github.dersolopes.eventmanagement.controller;

import com.github.dersolopes.eventmanagement.dto.LoginRequestDTO;
import com.github.dersolopes.eventmanagement.dto.RegisterRequestDTO;
import com.github.dersolopes.eventmanagement.dto.TokenResponseDTO;
import com.github.dersolopes.eventmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto) {
        log.info("Recebida requisição de registro para o e-mail: {}", dto.email());
        TokenResponseDTO response = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        log.info("Recebida requisição de login para o e-mail: {}", dto.email());
        TokenResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
}