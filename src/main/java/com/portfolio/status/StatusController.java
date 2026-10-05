package com.portfolio.status;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Homelab status. */
@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    /** Current state, 90 days of uptime and the most recent incident per monitored service. */
    @GetMapping
    public ResponseEntity<StatusDto> getStatus() {
        return ResponseEntity.ok().body(statusService.readStatus());
    }
}
