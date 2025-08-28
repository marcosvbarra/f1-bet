package com.f1bet.integration;

public record OpenF1SessionResult(
        Integer session_key,
        Integer driver_number,
        Integer driver_id
) {}
