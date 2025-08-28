package com.f1bet.controller.response;

import java.math.BigDecimal;

public record PlaceBetResponse(Long userId, Long betId, String status, BigDecimal betAmount, Integer odds, BigDecimal totalAwarded) {
}
