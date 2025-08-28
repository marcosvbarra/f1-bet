package com.f1bet.service;

import com.f1bet.controller.request.ProcessEventOutcomeRequest;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventOutcomeServiceTest {

    private BetRepository betRepository;
    private UserRepository userRepository;
    private EventOutcomeService service;

    @BeforeEach
    void setup() {
        betRepository = Mockito.mock(BetRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        service = new EventOutcomeService(betRepository, userRepository);
    }

    @Test
    void processEventOutcome_updatesStatusesAndCreditsWinners() {
        Bet betWin = new Bet();
        betWin.setId(1L);
        betWin.setUserId(10L);
        betWin.setEventId("event-1");
        betWin.setDriverId(44);
        betWin.setAmount(BigDecimal.valueOf(20.0));
        betWin.setOdds(3);
        betWin.setStatus(BetStatus.PENDING.name());

        Bet betLose = new Bet();
        betLose.setId(2L);
        betLose.setUserId(11L);
        betLose.setEventId("event-1");
        betLose.setDriverId(63);
        betLose.setAmount(BigDecimal.valueOf(15.0));
        betLose.setOdds(2);
        betLose.setStatus(BetStatus.PENDING.name());

        when(betRepository.findByEventIdAndStatus("event-1", BetStatus.PENDING.name()))
                .thenReturn(List.of(betWin, betLose));

        User winnerUser = new User();
        winnerUser.setId(10L);
        winnerUser.setBalance(BigDecimal.valueOf(50.0));
        when(userRepository.findById(10L)).thenReturn(Optional.of(winnerUser));

        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessEventOutcomeRequest request = new ProcessEventOutcomeRequest("event-1", 44);

        service.processEventOutcome(request);

        ArgumentCaptor<Bet> betCaptor = ArgumentCaptor.forClass(Bet.class);
        verify(betRepository, times(2)).save(betCaptor.capture());
        List<Bet> savedBets = betCaptor.getAllValues();
        assertTrue(savedBets.stream().anyMatch(b -> b.getId().equals(1L) && BetStatus.WON.name().equals(b.getStatus())));
        assertTrue(savedBets.stream().anyMatch(b -> b.getId().equals(2L) && BetStatus.LOST.name().equals(b.getStatus())));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals(110.0, userCaptor.getValue().getBalance().doubleValue(), 0.0001);
    }

    @Test
    void processEventOutcome_userMissingForWinner_throws404() {
        Bet betWin = new Bet();
        betWin.setId(1L);
        betWin.setUserId(10L);
        betWin.setEventId("event-1");
        betWin.setDriverId(44);
        betWin.setAmount(BigDecimal.valueOf(20.0));
        betWin.setOdds(3);
        betWin.setStatus(BetStatus.PENDING.name());

        when(betRepository.findByEventIdAndStatus("event-1", BetStatus.PENDING.name()))
                .thenReturn(List.of(betWin));

        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        ProcessEventOutcomeRequest request = new ProcessEventOutcomeRequest("event-1", 44);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.processEventOutcome(request));
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().toLowerCase().contains("user not found"));
    }

    @Test
    void processEventOutcome_noPendingBets_doesNothing() {
        when(betRepository.findByEventIdAndStatus("event-x", BetStatus.PENDING.name()))
                .thenReturn(Collections.emptyList());

        service.processEventOutcome(new ProcessEventOutcomeRequest("event-x", 99));

        verify(betRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void processEventOutcome_nullDriverId_marksLostAndSaves() {
        Bet bet = new Bet();
        bet.setId(7L);
        bet.setUserId(1L);
        bet.setEventId("event-y");
        bet.setDriverId(null);
        bet.setAmount(BigDecimal.valueOf(10.0));
        bet.setOdds(2);
        bet.setStatus(BetStatus.PENDING.name());

        when(betRepository.findByEventIdAndStatus("event-y", BetStatus.PENDING.name()))
                .thenReturn(List.of(bet));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.processEventOutcome(new ProcessEventOutcomeRequest("event-y", 44));

        ArgumentCaptor<Bet> captor = ArgumentCaptor.forClass(Bet.class);
        verify(betRepository).save(captor.capture());
        Bet saved = captor.getValue();
        assertEquals(BetStatus.LOST.name(), saved.getStatus());

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }
}
