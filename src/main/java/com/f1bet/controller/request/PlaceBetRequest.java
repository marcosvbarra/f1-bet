package com.f1bet.controller.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PlaceBetRequest(
        @NotNull(message = "userId is required")
        @Positive(message = "userId must be positive")
        Long userId,

        @NotBlank(message = "eventId must be provided")
        String eventId,

        @NotNull(message = "driverId is required")
        @Positive(message = "driverId must be positive")
        Integer driverId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than 0")
        BigDecimal amount
) {
}
