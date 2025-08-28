package com.f1bet.repository;

import com.f1bet.model.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

    List<Bet> findByEventIdAndStatus(String eventId, String status);

    List<Bet> findByEventId(String eventId);
}