package com.f1bet.service;

import com.f1bet.controller.response.EventResponse;
import com.f1bet.integration.F1APIClient;
import com.f1bet.mapper.EventMapper;
import com.f1bet.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final F1APIClient f1APIClient;
    private final EventMapper eventMapper;

    @Autowired
    public EventService(F1APIClient f1APIClient, EventMapper eventMapper) {
        this.f1APIClient = f1APIClient;
        this.eventMapper = eventMapper;
    }

    public List<EventResponse> getEvents(String sessionType, Integer year, String country, int page, int size) {
        int safeSize = Math.max(1, Math.min(30, size));
        int safePage = Math.max(0, page);

        List<Event> sessions = f1APIClient.getSessions(sessionType, year, country);
        if (sessions == null || sessions.isEmpty()) {
            return Collections.emptyList();
        }
        int fromIndex = safePage * safeSize;
        if (fromIndex >= sessions.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + safeSize, sessions.size());

        List<Event> pageItems = sessions.subList(fromIndex, toIndex);
        pageItems.forEach(session -> {
            boolean needsFetch = session.getDriverMarket() == null || session.getDriverMarket().isEmpty();
            if (needsFetch && session.getSessionKey() != null) {
                session.setDriverMarket(f1APIClient.getDriversForSession(session.getSessionKey()));
            }
        });

        return pageItems.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }
}
