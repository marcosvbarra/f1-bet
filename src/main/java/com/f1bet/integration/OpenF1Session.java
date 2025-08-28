package com.f1bet.integration;

public record OpenF1Session (
    Integer session_key,
    String session_name,
    String session_type,
    Integer year,
    String country_name
){}
