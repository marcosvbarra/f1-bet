package com.f1bet.integration;

import com.f1bet.model.Driver;
import com.f1bet.model.Event;

import java.util.Collections;
import java.util.List;

public interface F1APIClient {

    List<Event> getSessions(String sessionType, Integer year, String country);

    default List<Driver> getDriversForSession(Integer sessionKey) {
        return Collections.emptyList();
    }

    default List<OpenF1SessionResult> getSessionResults(Integer sessionKey) {
        return Collections.emptyList();
    }
}