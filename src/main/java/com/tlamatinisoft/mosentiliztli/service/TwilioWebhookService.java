package com.tlamatinisoft.mosentiliztli.service;

import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestStatus;
import com.tlamatinisoft.mosentiliztli.model.TwilioMessage;
import com.tlamatinisoft.mosentiliztli.repository.GuestRepository;
import com.tlamatinisoft.mosentiliztli.repository.TwilioMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TwilioWebhookService {
    private final TwilioMessageRepository twilioMessageRepository;
    private final GuestRepository guestRepository;

    public TwilioWebhookService(TwilioMessageRepository twilioMessageRepository, GuestRepository guestRepository) {
        this.twilioMessageRepository = twilioMessageRepository;
        this.guestRepository = guestRepository;
    }

    @Transactional
    public void handleIncomingMessage(String remitente, String cuerpoMensaje) {
        // Formato esperado de Twilio: whatsapp:+123456789
        String celular = remitente.replace("whatsapp:", "").trim();
        
        // Normalizar caso especial de México (+521 vs +52)
        if (celular.startsWith("+521") && celular.length() == 13) {
            celular = "+52" + celular.substring(4);
        }

        Guest guest = guestRepository.findByCelular(celular).orElse(null);

        TwilioMessage message = new TwilioMessage();
        message.setNumeroRemitente(celular);
        message.setGuest(guest);
        message.setCuerpoMensaje(cuerpoMensaje);
        message.setFechaRecepcion(LocalDateTime.now());

        if (guest != null) {
            if (guest.getEstatus() != GuestStatus.DECLINADO && guest.getEstatus() != GuestStatus.CONFIRMADO) {
                guest.setEstatus(GuestStatus.RESPONDIO_WHATSAPP);
                guestRepository.save(guest);
            }
        }

        twilioMessageRepository.save(message);
    }
}
