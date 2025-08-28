package com.f1bet.mapper;

import com.f1bet.controller.response.PlaceBetResponse;
import com.f1bet.model.Bet;
import org.springframework.stereotype.Component;

@Component
public class BetMapper {

    public PlaceBetResponse toResponse(Bet bet) {
        return new PlaceBetResponse(
                bet.getUserId(),
                bet.getId(),
                bet.getStatus(),
                bet.getAmount(),
                bet.getOdds(),
                bet.getTotalAwarded()
        );
    }
}
