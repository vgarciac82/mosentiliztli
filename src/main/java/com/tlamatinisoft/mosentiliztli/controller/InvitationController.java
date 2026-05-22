package com.tlamatinisoft.mosentiliztli.controller;

import com.tlamatinisoft.mosentiliztli.dto.GuestResponse;
import com.tlamatinisoft.mosentiliztli.dto.LoginRequest;
import com.tlamatinisoft.mosentiliztli.dto.RsvpRequest;
import com.tlamatinisoft.mosentiliztli.service.GuestService;
import com.tlamatinisoft.mosentiliztli.service.RsvpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class InvitationController {

    private final GuestService guestService;
    private final RsvpService rsvpService;

    public InvitationController(GuestService guestService, RsvpService rsvpService) {
        this.guestService = guestService;
        this.rsvpService = rsvpService;
    }

    @GetMapping("/invitation/{token}")
    public ResponseEntity<GuestResponse> getInvitationByToken(@PathVariable String token) {
        return ResponseEntity.ok(guestService.getGuestByToken(token));
    }

    @PostMapping("/invitation/login")
    public ResponseEntity<GuestResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(guestService.getGuestByCodigo(request.getCodigoInvitacion()));
    }

    @PostMapping("/rsvp")
    public ResponseEntity<Void> processRsvp(@Valid @RequestBody RsvpRequest request) {
        rsvpService.processRsvp(request);
        return ResponseEntity.ok().build();
    }
}
