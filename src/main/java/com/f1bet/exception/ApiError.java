package com.f1bet.exception;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path
) {}
