package com.github.dersolopes.eventmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponseDTO(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponseDTO of(int status, String error, String message, String path) {
        return new ApiErrorResponseDTO(status, error, message, path, LocalDateTime.now(), null);
    }

    public static ApiErrorResponseDTO of(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ApiErrorResponseDTO(status, error, message, path, LocalDateTime.now(), fieldErrors);
    }
}