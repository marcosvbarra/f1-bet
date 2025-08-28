package com.f1bet.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProcessEventOutcomeRequest(
        @NotBlank(message = "eventId must be provided")
        String eventId,
        @Positive(message = "winningDriverId must be positive")
        Integer winningDriverId
) {
}
