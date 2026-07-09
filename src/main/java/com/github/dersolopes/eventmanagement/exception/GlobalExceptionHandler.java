package com.github.dersolopes.eventmanagement.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador global para interceptação e tratamento de exceções da aplicação.
 * Centraliza as respostas de erro mapeando exceções técnicas e de negócio
 * para os respectivos status HTTP semânticos usando mapas dinâmicos.
 *
 * @author dersolopes
 */
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * Captura violações de integridade do banco de dados (ex: chaves duplicadas / UNIQUE constraint).
     * Traduz uma falha técnica de persistência em uma resposta semântica de conflito.
     *
     * @param ex A exceção de integridade disparada pelo Spring Data / Hibernate
     * @return {@link ResponseEntity} contendo o mapa de erro com status 409 Conflict
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflito de Dados");
        body.put("message", "Participante já está inscrito neste evento!");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Captura exceções associadas a quebras de regras de negócio (ex: evento com capacidade máxima atingida).
     * Retorna a mensagem customizada definida no momento do disparo da exceção.
     *
     * @param ex A exceção de regra de negócio customizada da aplicação
     * @return {@link ResponseEntity} contendo o mapa de erro com status 409 Conflict
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Regra de Negócio");
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Captura exceções de recursos não encontrados (ex: busca por IDs inexistentes de eventos ou usuários).
     * Retorna a mensagem customizada informando qual recurso não foi localizado.
     *
     * @param ex A exceção de recurso não encontrado customizada da aplicação
     * @return {@link ResponseEntity} contendo o mapa de erro com status 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Não Encontrado");
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
