package com.f1bet.service;

import com.f1bet.controller.request.PlaceBetRequest;
import com.f1bet.controller.response.PlaceBetResponse;
import com.f1bet.integration.F1APIClient;
import com.f1bet.integration.OpenF1SessionResult;
import com.f1bet.mapper.BetMapper;
import com.f1bet.model.Bet;
import com.f1bet.model.BetStatus;
import com.f1bet.model.User;
import com.f1bet.repository.BetRepository;
import com.f1bet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BetServiceTest {

    private BetRepository betRepository;
    private UserRepository userRepository;
    private F1APIClient f1APIClient;
    private BetService betService;
    private BetMapper betMapper;

    @BeforeEach
    void setUp() {
        betRepository = Mockito.mock(BetRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        f1APIClient = Mockito.mock(F1APIClient.class);
        betService = new BetService(betRepository, userRepository, f1APIClient, betMapper);
    }

    @Test
    void placeBet_success_persistsBetAndUpdatesBalance() {
        long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(100.0));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> {
            Bet b = invocation.getArgument(0);
            b.setId(10L);
            return b;
        });

        when(f1APIClient.getSessionResults(7782)).thenReturn(List.of(new OpenF1SessionResult(7782, 44, null)));
        PlaceBetRequest request = new PlaceBetRequest(userId, "7782", 44, BigDecimal.valueOf(25.0));

        PlaceBetResponse response = betService.placeBet(request);

        assertNotNull(response);
        assertEquals(10L, response.betId());
        assertEquals(BetStatus.PENDING.name(), response.status());

        verify(userRepository, times(1)).save(any(User.class));

        ArgumentCaptor<Bet> betCaptor = ArgumentCaptor.forClass(Bet.class);
        verify(betRepository, times(1)).save(betCaptor.capture());
        Bet persisted = betCaptor.getValue();
        assertEquals(userId, persisted.getUserId());
        assertEquals("7782", persisted.getEventId());
        assertEquals(Integer.valueOf(44), persisted.getDriverId());
        assertEquals(0, persisted.getAmount().compareTo(BigDecimal.valueOf(25.0)));
        assertTrue(persisted.getOdds() >= 2 && persisted.getOdds() <= 4);
        assertEquals(BetStatus.PENDING.name(), persisted.getStatus());
    }

    @Test
    void placeBet_userNotFound_throws404() {
        long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        PlaceBetRequest request = new PlaceBetRequest(userId, "1", 1, BigDecimal.valueOf(10.0));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> betService.placeBet(request));
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().toLowerCase().contains("user not found"));
        verifyNoInteractions(betRepository);
    }

    @Test
    void placeBet_insufficientBalance_throws400() {
        long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(5.0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(f1APIClient.getSessionResults(1)).thenReturn(List.of(new OpenF1SessionResult(1, 1, null)));
        PlaceBetRequest request = new PlaceBetRequest(userId, "1", 1, BigDecimal.valueOf(10.0));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> betService.placeBet(request));
        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().toLowerCase().contains("insufficient balance"));
        verify(betRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }


    @Test
    void placeBet_eventNotFound_throws404() {
        long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(100));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(f1APIClient.getSessionResults(1234)).thenReturn(List.of());

        PlaceBetRequest request = new PlaceBetRequest(userId, "1234", 44, BigDecimal.TEN);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> betService.placeBet(request));
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().toLowerCase().contains("event not found"));
        verify(betRepository, never()).save(any());
    }

    @Test
    void placeBet_driverNotInEvent_throws400() {
        long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(100));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(f1APIClient.getSessionResults(5000)).thenReturn(List.of(new OpenF1SessionResult(5000, 99, 999)));

        PlaceBetRequest request = new PlaceBetRequest(userId, "5000", 44, BigDecimal.TEN);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> betService.placeBet(request));
        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().toLowerCase().contains("driver not part"));
        verify(betRepository, never()).save(any());
    }

    @Test
    void getBetsByEventId_mapsAllFields() {
        Bet bet = new Bet(1L, "e1", 44, BigDecimal.valueOf(12.5), 3, "PENDING");
        bet.setId(77L);
        bet.setTotalAwarded(BigDecimal.ZERO);

        when(betRepository.findByEventId("e1")).thenReturn(List.of(bet));

        var responses = betService.getBetsByEventId("e1");
        assertEquals(1, responses.size());
        PlaceBetResponse r = responses.getFirst();
        assertEquals(1L, r.userId());
        assertEquals(77L, r.betId());
        assertEquals("PENDING", r.status());
        assertEquals(0, r.betAmount().compareTo(BigDecimal.valueOf(12.5)));
        assertEquals(3, r.odds());
        assertEquals(0, r.totalAwarded().compareTo(BigDecimal.ZERO));
    }
}
