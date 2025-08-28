package com.f1bet.mapper;

import com.f1bet.controller.response.DriverResponse;
import com.f1bet.controller.response.EventResponse;
import com.f1bet.model.Driver;
import com.f1bet.model.Event;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    public EventResponse toResponse(Event event) {
        List<DriverResponse> driverMarket = (event.getDriverMarket() == null ? List.<Driver>of() : event.getDriverMarket())
                .stream()
                .map(this::toDriverResponse)
                .collect(Collectors.toList());

        return new EventResponse(
                event.getSessionKey(),
                event.getSessionName(),
                event.getSessionType(),
                event.getYear(),
                event.getCountry(),
                driverMarket
        );
    }

    public DriverResponse toDriverResponse(Driver driver) {
        return new DriverResponse(driver.getFullName(), driver.getDriverNumber(), driver.getOdds());
    }
}
