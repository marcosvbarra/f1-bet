package com.f1bet.controller.response;

import java.util.List;

public record EventResponse(Integer sessionKey, String sessionName, String sessionType, Integer year, String country, List<DriverResponse> driverMarket) {
}
