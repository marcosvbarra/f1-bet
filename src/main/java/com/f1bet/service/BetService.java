package com.f1bet.service;

import com.f1bet.controller.request.PlaceBetRequest;
import com.f1bet.controller.response.PlaceBetResponse;
import com.f1bet.integration.OpenF1Client;
import com.f1bet.integration.OpenF1SessionResult;
import com.f1bet.mapper.BetMapper;
import com.f1bet.model.Bet;
import com.f1bet.model.BetStatus;
import com.f1bet.model.User;
import com.f1bet.repository.BetRepository;
import com.f1bet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class BetService {

    private final BetRepository betRepository;
    private final UserRepository userRepository;
    private final OpenF1Client openF1Client;
    private final BetMapper betMapper;

    @Autowired
    public BetService(BetRepository betRepository, UserRepository userRepository, OpenF1Client openF1Client, BetMapper betMapper) {
        this.betRepository = betRepository;
        this.userRepository = userRepository;
        this.openF1Client = openF1Client;
        this.betMapper = betMapper;
    }


    @Transactional
    public PlaceBetResponse placeBet(PlaceBetRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        int sessionKey = Integer.parseInt(request.eventId());

        List<OpenF1SessionResult> sessionResults = openF1Client.getSessionResults(sessionKey);
        if (sessionResults == null || sessionResults.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found for session key: " + sessionKey);
        }
        boolean driverFound = sessionResults.stream().anyMatch(sessionResult -> {
            Integer driverNumber = sessionResult.driver_number();
            Integer driverId = sessionResult.driver_id();
            return (driverNumber != null && driverNumber.equals(request.driverId())) || (driverId != null && driverId.equals(request.driverId()));
        });
        if (!driverFound) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver not part of this event");
        }

        BigDecimal amount = request.amount();
        if (user.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        Bet bet = new Bet(
                request.userId(),
                request.eventId(),
                request.driverId(),
                request.amount(),
                new Random().nextInt(3) + 2,
                BetStatus.PENDING.name()
        );

        Bet savedBet = betRepository.save(bet);

        return betMapper.toResponse(savedBet);
    }

    public List<PlaceBetResponse> getBetsByEventId(String eventId) {
        List<Bet> bets = betRepository.findByEventId(eventId);
        return bets.stream()
                .map(betMapper::toResponse)
                .collect(Collectors.toList());
    }
}