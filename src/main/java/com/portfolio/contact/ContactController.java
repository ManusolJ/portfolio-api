package com.portfolio.contact;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** Public contact form. */
@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    private final MailService mailService;

    public ContactController(MailService mailService) {
        this.mailService = mailService;
    }

    /** Sends the message and answers 204; validation failures answer 400. */
    @PostMapping
    public ResponseEntity<Void> contact(@RequestBody @Valid ContactRequestDto request) {
        mailService.sendContactEmail(request);

        return ResponseEntity.noContent().build();
    }
}
