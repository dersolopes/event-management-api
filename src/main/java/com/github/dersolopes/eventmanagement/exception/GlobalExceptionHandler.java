package com.github.dersolopes.eventmanagement.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador global para interceptação e tratamento de exceções da aplicação.
 * Centraliza as respostas de erro mapeando exceções técnicas e de negócio
 * para DTOs padronizados (ProblemDetailResponse) com status HTTP semânticos.
 *
 * @author dersolopes
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura exceções de validação dos DTOs disparadas pelo @Valid no Controller.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());

            // Log limpo e individual por campo com falha de validação
            log.warn("Erro de validação no campo '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
        }

        ProblemDetailResponse response = new ProblemDetailResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição Inválida",
                "Um ou mais campos estão inválidos. Corrija o formulário e tente novamente.",
                LocalDateTime.now(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Captura violações de integridade do banco de dados (ex: chaves duplicadas / UNIQUE constraint).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetailResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade no banco de dados: {}", ex.getMostSpecificCause().getMessage());

        ProblemDetailResponse response = new ProblemDetailResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito de Dados",
                "O registro já existe no sistema ou possui dependências ativas vinculadas.",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Captura exceções associadas a quebras de regras de negócio.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetailResponse> handleBusiness(BusinessException ex) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());

        ProblemDetailResponse response = new ProblemDetailResponse(
                HttpStatus.CONFLICT.value(),
                "Regra de Negócio Violada",
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Captura exceções de recursos não encontrados (ex: IDs inexistentes).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());

        ProblemDetailResponse response = new ProblemDetailResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso Não Encontrado",
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Captura exceções genéricas não tratadas (fallback para erros 500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailResponse> handleUncaughtException(Exception ex) {
        log.error("Erro interno não tratado no servidor: ", ex);

        ProblemDetailResponse response = new ProblemDetailResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno no Servidor",
                "Ocorreu um erro inesperado. Entre em contato com o suporte se o problema persistir.",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetailResponse> handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetailResponse problem = new ProblemDetailResponse(
                ex.getStatusCode().value(),
                "Recurso Não Encontrado",
                ex.getReason() != null ? ex.getReason() : ex.getMessage(),
                LocalDateTime.now(),
                null // fieldErrors é null para erros de recurso não encontrado
        );
        return ResponseEntity.status(ex.getStatusCode()).body(problem);
    }

    /**
     * Captura falhas de autenticação (ex: senha incorreta ou usuário inexistente).
     * Retorna HTTP 401 Unauthorized.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ProblemDetailResponse> handleBadCredentials(Exception ex) {
        log.warn("Falha de autenticação: credenciais inválidas fornecidas. Motivo: {}", ex.getMessage());

        ProblemDetailResponse response = new ProblemDetailResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Não Autorizado",
                "Credenciais inválidas. Verifique seu e-mail e senha.",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Captura tentativas de acesso negado por falta de privilégios/roles.
     * Retorna HTTP 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetailResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado para o recurso solicitado: {}", ex.getMessage());

        ProblemDetailResponse response = new ProblemDetailResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acesso Negado",
                "Você não possui permissão para acessar este recurso.",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

}