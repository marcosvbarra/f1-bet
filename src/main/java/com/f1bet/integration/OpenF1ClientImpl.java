package com.f1bet.integration;

import com.f1bet.model.Driver;
import com.f1bet.model.Event;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class OpenF1ClientImpl implements F1APIClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String openF1BaseUrl = "https://api.openf1.org/v1";

    private static final int MAX_REQ_PER_SEC = 3;
    private static final Semaphore RATE_SEMAPHORE = new Semaphore(MAX_REQ_PER_SEC, true);
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "OpenF1-RateLimiter");
        t.setDaemon(true);
        return t;
    });
    static {
        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                int toRelease = MAX_REQ_PER_SEC - RATE_SEMAPHORE.availablePermits();
                if (toRelease > 0) {
                    RATE_SEMAPHORE.release(toRelease);
                }
            } catch (Exception ignored) {

            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void acquirePermit() {
        try {
            RATE_SEMAPHORE.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter", e);
        }
    }

    @Override
    public List<Event> getSessions(String sessionType, Integer year, String country) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(openF1BaseUrl + "/sessions"));

        if (sessionType != null) {
            builder.queryParam("session_type", sessionType);
        }
        if (year != null) {
            builder.queryParam("year", year);
        }
        if (country != null) {
            builder.queryParam("country_name", country);
        }

        acquirePermit();
        ResponseEntity<List<OpenF1Session>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        List<OpenF1Session> sessions = response.getBody();

        if (sessions == null) {
            return new ArrayList<>();
        }
        return sessions.stream()
                .map(this::toEvent)
                .collect(Collectors.toList());
    }

    private Event toEvent(OpenF1Session session){
        Event event = new Event();
        event.setSessionKey(session.session_key());
        event.setSessionName(session.session_name());
        event.setSessionType(session.session_type());
        event.setYear(session.year());
        event.setCountry(session.country_name());
        return event;
    }

    @Override
    public List<Driver> getDriversForSession(Integer sessionKey) {
        if (sessionKey == null) {
            return new ArrayList<>();
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(openF1BaseUrl + "/drivers"))
                .queryParam("session_key", sessionKey);

        acquirePermit();
        ResponseEntity<List<OpenF1Driver>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        List<OpenF1Driver> drivers = response.getBody();
        if (drivers == null) {
            return new ArrayList<>();
        }
        return drivers.stream()
                .map(d -> {
                    Driver driver = new Driver();
                    driver.setFullName(d.full_name());
                    driver.setDriverNumber(d.driver_number() != null ? d.driver_number() : 0);
                    int odds = new Random().nextInt(3) + 2;
                    driver.setOdds(odds);
                    return driver;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OpenF1SessionResult> getSessionResults(Integer sessionKey) {
        if (sessionKey == null) {
            return new ArrayList<>();
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(openF1BaseUrl + "/session_result"))
                .queryParam("session_key", sessionKey);

        acquirePermit();
        ResponseEntity<List<OpenF1SessionResult>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        List<OpenF1SessionResult> results = response.getBody();
        return results != null ? results : new ArrayList<>();
    }

}