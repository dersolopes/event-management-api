package com.github.dersolopes.eventmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Desabilita o CSRF completamente para aceitar POST/PUT externos
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configura a API para ser STATLESS (sem guardar sessões no servidor)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Libera as rotas de teste
                // Adicionamos o /error aqui para permitir que o Spring Security deixe
                // as respostas do GlobalExceptionHandler passarem limpas enquanto nao implementamos o JWT
                // TODO - Deletar após implementar JWT
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
