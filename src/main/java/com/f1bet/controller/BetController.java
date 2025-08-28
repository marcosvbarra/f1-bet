package com.f1bet.controller;

import com.f1bet.controller.request.PlaceBetRequest;
import com.f1bet.controller.response.PlaceBetResponse;
import com.f1bet.service.BetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bets")
public class BetController {

    private final BetService betService;

    public BetController(BetService betService) {
        this.betService = betService;
    }

    @PostMapping
    public PlaceBetResponse placeBet(@Valid @RequestBody PlaceBetRequest request) {
        return betService.placeBet(request);
    }

    @GetMapping
    public List<PlaceBetResponse> getBetsByEventId(@RequestParam("event_id") String eventId) {
        return betService.getBetsByEventId(eventId);
    }
}
