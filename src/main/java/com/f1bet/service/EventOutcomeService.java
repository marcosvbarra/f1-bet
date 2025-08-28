package com.f1bet.service;

import com.f1bet.controller.request.ProcessEventOutcomeRequest;
import com.f1bet.model.Bet;
import com.f1bet.model.BetStatus;
import com.f1bet.model.User;
import com.f1bet.repository.BetRepository;
import com.f1bet.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EventOutcomeService {

    private final BetRepository betRepository;
    private final UserRepository userRepository;

    public EventOutcomeService(BetRepository betRepository, UserRepository userRepository) {
        this.betRepository = betRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void processEventOutcome(ProcessEventOutcomeRequest request) {
        List<Bet> bets = betRepository.findByEventIdAndStatus(request.eventId(), BetStatus.PENDING.name());

        bets.forEach(bet -> {
            if (bet.getDriverId() != null && bet.getDriverId().equals(request.winningDriverId())) {
                bet.setStatus(BetStatus.WON.name());
                BigDecimal prize = bet.getAmount().multiply(BigDecimal.valueOf(bet.getOdds()));
                bet.setTotalAwarded(prize);
                updateUserBalance(bet.getUserId(), prize);
            } else {
                bet.setStatus(BetStatus.LOST.name());
                bet.setTotalAwarded(BigDecimal.ZERO);
            }
            betRepository.save(bet);
        });
    }

    private void updateUserBalance(Long userId, BigDecimal prize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setBalance(user.getBalance().add(prize));
        userRepository.save(user);
    }
}
