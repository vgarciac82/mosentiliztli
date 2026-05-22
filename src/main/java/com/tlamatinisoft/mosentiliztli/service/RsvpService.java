package com.tlamatinisoft.mosentiliztli.service;

import com.tlamatinisoft.mosentiliztli.dto.RsvpRequest;
import com.tlamatinisoft.mosentiliztli.exception.ResourceNotFoundException;
import com.tlamatinisoft.mosentiliztli.exception.RsvpValidationException;
import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestStatus;
import com.tlamatinisoft.mosentiliztli.model.RsvpResponse;
import com.tlamatinisoft.mosentiliztli.repository.GuestRepository;
import com.tlamatinisoft.mosentiliztli.repository.RsvpResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RsvpService {
    private final GuestRepository guestRepository;
    private final RsvpResponseRepository rsvpResponseRepository;

    public RsvpService(GuestRepository guestRepository, RsvpResponseRepository rsvpResponseRepository) {
        this.guestRepository = guestRepository;
        this.rsvpResponseRepository = rsvpResponseRepository;
    }

    @Transactional
    public void processRsvp(RsvpRequest request) {
        Guest guest = guestRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invitado no encontrado"));

        if (guest.getEstatus() == GuestStatus.DECLINADO) {
            throw new RsvpValidationException("RSVP_LOCKED", "Tu invitación ha sido declinada previamente y no puede ser modificada.");
        }

        if (request.getPasesConfirmados() > guest.getPasesAsignados()) {
            throw new RsvpValidationException("RSVP_LIMIT_EXCEEDED", "No puedes confirmar más pases de los asignados.");
        }

        guest.setEstatus(request.getStatusDecision());
        guestRepository.save(guest);

        RsvpResponse response = new RsvpResponse();
        response.setGuest(guest);
        response.setPasesConfirmados(request.getStatusDecision() == GuestStatus.DECLINADO ? 0 : request.getPasesConfirmados());
        response.setFechaConfirmacion(LocalDateTime.now());
        rsvpResponseRepository.save(response);
    }
}
