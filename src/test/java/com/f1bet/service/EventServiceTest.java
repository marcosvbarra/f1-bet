package com.f1bet.service;

import com.f1bet.integration.F1APIClient;
import com.f1bet.controller.response.DriverResponse;
import com.f1bet.controller.response.EventResponse;
import com.f1bet.mapper.EventMapper;
import com.f1bet.model.Driver;
import com.f1bet.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class EventServiceTest {


    private F1APIClient client;
    private EventService service;
    private EventMapper eventMapper;

    @BeforeEach
    void setup() {
        client = Mockito.mock(F1APIClient.class);
        service = new EventService(client,eventMapper);
    }

    @Test
    void getEvents_empty_returnsEmptyList() {
        when(client.getSessions(eq(null), eq(null), eq(null))).thenReturn(List.of());
        List<EventResponse> responses = service.getEvents(null, null, null, 0, 10);
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getEvents_delegatesToClientAndMapsResponse() {
        Event e = new Event();
        e.setSessionKey(123);
        e.setSessionName("Qualifying");
        e.setSessionType("Q");
        e.setYear(2024);
        e.setCountry("Italy");
        Driver d1 = new Driver();
        d1.setFullName("Max Verstappen");
        d1.setDriverNumber(1);
        d1.setOdds(3);
        Driver d2 = new Driver();
        d2.setFullName("Charles Leclerc");
        d2.setDriverNumber(16);
        d2.setOdds(2);
        e.setDriverMarket(List.of(d1, d2));

        when(client.getSessions(eq("Q"), eq(2024), eq("Italy"))).thenReturn(List.of(e));

        List<EventResponse> responses = service.getEvents("Q", 2024, "Italy", 0, 10);

        assertEquals(1, responses.size());
        EventResponse eventResponse = responses.getFirst();
        assertEquals(123, eventResponse.sessionKey());
        assertEquals("Qualifying", eventResponse.sessionName());
        assertEquals("Q", eventResponse.sessionType());
        assertEquals(2024, eventResponse.year());
        assertEquals("Italy", eventResponse.country());
        List<DriverResponse> market = eventResponse.driverMarket();
        assertEquals(2, market.size());
        assertEquals("Max Verstappen", market.getFirst().fullName());
        assertEquals(1, market.get(0).driverNumber());
        assertEquals(3, market.get(0).odds());
        assertEquals("Charles Leclerc", market.get(1).fullName());
        assertEquals(16, market.get(1).driverNumber());
        assertEquals(2, market.get(1).odds());
    }
    @Test
    void getEvents_nullSessions_returnsEmptyList() {
        when(client.getSessions(eq(null), eq(null), eq(null))).thenReturn(null);
        List<?> responses = service.getEvents(null, null, null, 0, 10);
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getEvents_pagination_outOfRange_returnsEmpty() {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Event e = new Event();
            e.setSessionKey(i);
            e.setSessionName("S" + i);
            e.setDriverMarket(List.of());
            events.add(e);
        }
        when(client.getSessions(eq(null), eq(null), eq(null))).thenReturn(events);

        List<?> responses = service.getEvents(null, null, null, 10, 10);
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getEvents_capsPageSizeTo30_andDoesNotFetchDriversIfMarketPresent() {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 35; i++) {
            Event e = new Event();
            e.setSessionKey(1000 + i);
            e.setSessionName("Session" + i);
            Driver d = new Driver();
            d.setFullName("Driver" + i);
            d.setDriverNumber(i);
            d.setOdds(2);
            e.setDriverMarket(List.of(d));
            events.add(e);
        }
        when(client.getSessions(eq("R"), eq(2024), eq("Italy"))).thenReturn(events);

        List<?> responses = service.getEvents("R", 2024, "Italy", -1, 100);
        assertEquals(30, responses.size());

        Mockito.verify(client, never()).getDriversForSession(anyInt());
    }

    @Test
    void getEvents_fetchesDriversWhenMarketMissing() {
        Event e = new Event();
        e.setSessionKey(999);
        e.setSessionName("Race");
        e.setDriverMarket(null);

        Driver d = new Driver();
        d.setFullName("Fetched Driver");
        d.setDriverNumber(22);
        d.setOdds(5);

        when(client.getSessions(eq(null), eq(null), eq(null))).thenReturn(List.of(e));
        when(client.getDriversForSession(999)).thenReturn(List.of(d));

        var responses = service.getEvents(null, null, null, 0, 10);
        assertEquals(1, responses.size());
        assertEquals(1, responses.getFirst().driverMarket().size());
        assertEquals("Fetched Driver", responses.getFirst().driverMarket().getFirst().fullName());
    }
}
