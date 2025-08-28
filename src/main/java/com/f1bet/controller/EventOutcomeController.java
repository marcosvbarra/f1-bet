package com.f1bet.controller;

import com.f1bet.controller.request.ProcessEventOutcomeRequest;
import com.f1bet.service.EventOutcomeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event-outcomes")
public class EventOutcomeController {

    private final EventOutcomeService eventOutcomeService;

    public EventOutcomeController(EventOutcomeService eventOutcomeService) {
        this.eventOutcomeService = eventOutcomeService;
    }

    @PostMapping
    public ResponseEntity<Void> processEventOutcome(@Valid @RequestBody ProcessEventOutcomeRequest request) {
        eventOutcomeService.processEventOutcome(request);
        return ResponseEntity.ok().build();
    }
}
