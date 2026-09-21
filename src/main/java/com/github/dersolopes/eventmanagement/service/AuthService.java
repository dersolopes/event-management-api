package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.dto.LoginRequestDTO;
import com.github.dersolopes.eventmanagement.dto.RegisterRequestDTO;
import com.github.dersolopes.eventmanagement.dto.TokenResponseDTO;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    @Transactional
    public TokenResponseDTO register(RegisterRequestDTO dto);

    public TokenResponseDTO login(LoginRequestDTO dto);

}