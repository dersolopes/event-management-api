package com.github.dersolopes.eventmanagement.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ProblemDetailResponse(
        int status,
        String title,
        String detail,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {}