package com.tlamatinisoft.mosentiliztli.controller;

import com.tlamatinisoft.mosentiliztli.service.TwilioWebhookService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
public class TwilioWebhookController {

    private final TwilioWebhookService twilioWebhookService;

    public TwilioWebhookController(TwilioWebhookService twilioWebhookService) {
        this.twilioWebhookService = twilioWebhookService;
    }

    @PostMapping(value = "/twilio", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> handleTwilioWebhook(
            @RequestParam("From") String from,
            @RequestParam("Body") String body) {
        
        twilioWebhookService.handleIncomingMessage(from, body);
        
        String responseMessage = "<Response><Message>"
                + "¡Hola! 💍 Gracias por escribirnos.\n\n"
                + "Este es un número automático configurado para tus invitaciones y confirmación de asistencia.\n\n"
                + "Si quieres mandarnos un mensaje personal o tienes alguna duda, escríbenos directamente a nuestros números personales tocando aquí:\n\n"
                + "🤵🏻‍♂️ Vicente: https://wa.me/523330698723\n"
                + "👰🏻‍♀️ Jenny: https://wa.me/523313203746"
                + "</Message></Response>";

        return ResponseEntity.ok(responseMessage);
    }
}
