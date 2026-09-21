package com.github.dersolopes.eventmanagement.service.impl;

import com.github.dersolopes.eventmanagement.dto.LoginRequestDTO;
import com.github.dersolopes.eventmanagement.dto.RegisterRequestDTO;
import com.github.dersolopes.eventmanagement.dto.TokenResponseDTO;
import com.github.dersolopes.eventmanagement.entity.User;
import com.github.dersolopes.eventmanagement.enums.Role;
import com.github.dersolopes.eventmanagement.exception.BusinessException;
import com.github.dersolopes.eventmanagement.repository.UserRepository;
import com.github.dersolopes.eventmanagement.security.JwtService;
import com.github.dersolopes.eventmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public TokenResponseDTO register(RegisterRequestDTO dto) {
        log.debug("Processando registro de novo usuário");

        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Falha no registro: e-mail já cadastrado");
            throw new BusinessException("E-mail já cadastrado no sistema.");
        }

        Role userRole = dto.role() != null ? dto.role() : Role.PARTICIPANT;

        User newUser = User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(userRole)
                .build();

        userRepository.save(newUser);
        log.info("Usuário registrado com sucesso ID: {}", newUser.getId());

        String token = jwtService.generateToken(newUser);
        return new TokenResponseDTO(
                token,
                newUser.getId(),
                newUser.getEmail(),
                newUser.getRole().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponseDTO login(LoginRequestDTO dto) {
        log.debug("Processando tentativa de autenticação");

        var authToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        Authentication auth = authenticationManager.authenticate(authToken);

        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user);

        log.info("Autenticação concluída com sucesso para o ID: {}", user.getId());
        return new TokenResponseDTO(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}