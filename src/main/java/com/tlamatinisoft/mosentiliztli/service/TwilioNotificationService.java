package com.tlamatinisoft.mosentiliztli.service;

import com.tlamatinisoft.mosentiliztli.model.EventConfig;
import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.repository.EventConfigRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class TwilioNotificationService {

    private final EventConfigRepository eventConfigRepository;
    
    private static final String CONTENT_SID = "HX0c5645b01788b9a6b78deedfc4299562";

    public TwilioNotificationService(EventConfigRepository eventConfigRepository) {
        this.eventConfigRepository = eventConfigRepository;
    }

    public String sendInvitation(Guest guest) {
        EventConfig config = eventConfigRepository.findById(1L).orElse(null);
        if (config == null || config.getTwilioAccountSid() == null || config.getTwilioAccountSid().isEmpty()) {
            throw new IllegalStateException("Twilio no está configurado en la base de datos.");
        }
        
        // Inicializar Twilio de manera segura
        Twilio.init(config.getTwilioAccountSid(), config.getTwilioAuthToken());
        
        String toPhone = guest.getCelular();
        if (toPhone != null && !toPhone.startsWith("+")) {
            toPhone = "+" + toPhone;
        }
        
        // El boton en la plantilla Twilio tiene base URL https://mywedding.tlamatinisoft.com/
        // La variable {{2}} es el path de la invitación. Usaremos el token (UUID).
        String contentVariables = String.format("{\"1\":\"%s\",\"2\":\"invitacion/%s\"}", 
            guest.getNombreInvitado(), 
            guest.getToken());

        String fromPhone = config.getTwilioPhoneNumber();
        if (fromPhone != null && !fromPhone.startsWith("whatsapp:")) {
            fromPhone = "whatsapp:" + fromPhone;
        }

        Message message = Message.creator(
                new PhoneNumber("whatsapp:" + toPhone),
                new PhoneNumber(fromPhone),
                "") // El Body es ignorado cuando se usa contentSid
            .setContentSid(CONTENT_SID)
            .setContentVariables(contentVariables)
            .create();

        return message.getSid();
    }
}
