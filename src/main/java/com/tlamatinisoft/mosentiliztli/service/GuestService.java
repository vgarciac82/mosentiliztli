package com.tlamatinisoft.mosentiliztli.service;

import com.tlamatinisoft.mosentiliztli.dto.GuestResponse;
import com.tlamatinisoft.mosentiliztli.exception.ResourceNotFoundException;
import com.tlamatinisoft.mosentiliztli.exception.RsvpValidationException;
import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.repository.EventConfigRepository;
import com.tlamatinisoft.mosentiliztli.repository.GuestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GuestService {
    private final GuestRepository guestRepository;
    private final EventConfigRepository eventConfigRepository;

    public GuestService(GuestRepository guestRepository, EventConfigRepository eventConfigRepository) {
        this.guestRepository = guestRepository;
        this.eventConfigRepository = eventConfigRepository;
    }

    public GuestResponse getGuestByToken(String token) {
        validateExpiration();
        Guest guest = guestRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitado no encontrado con el token proporcionado"));
        return GuestResponse.fromEntity(guest);
    }

    public GuestResponse getGuestByCodigo(String codigoInvitacion) {
        validateExpiration();
        Guest guest = guestRepository.findByCodigoInvitacion(codigoInvitacion)
                .orElseThrow(() -> new ResourceNotFoundException("Código de invitación inválido"));
        return GuestResponse.fromEntity(guest);
    }

    private void validateExpiration() {
        eventConfigRepository.findById(1L).ifPresent(config -> {
            if (config.getExpirationDate() != null && LocalDateTime.now().isAfter(config.getExpirationDate())) {
                throw new RsvpValidationException("RSVP_EXPIRED", "El tiempo límite para confirmar tu asistencia ha finalizado.");
            }
        });
    }
}
