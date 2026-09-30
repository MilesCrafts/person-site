package com.lekang.journal.message.api;

import com.lekang.journal.message.application.GuestMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
public class GuestMessageController {
    private final GuestMessageService service;

    public GuestMessageController(GuestMessageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GuestMessageCreatedResponse> create(
        @Valid @RequestBody GuestMessageCreateRequest request,
        HttpServletRequest servletRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(request, servletRequest.getRemoteAddr()));
    }
}
