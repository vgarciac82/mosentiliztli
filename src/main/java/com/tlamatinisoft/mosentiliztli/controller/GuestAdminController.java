package com.tlamatinisoft.mosentiliztli.controller;

import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.repository.GuestRepository;
import com.tlamatinisoft.mosentiliztli.service.GuestImportService;
import com.tlamatinisoft.mosentiliztli.service.TwilioNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/guests")
public class GuestAdminController {

    private final GuestImportService guestImportService;
    private final TwilioNotificationService twilioNotificationService;
    private final GuestRepository guestRepository;

    public GuestAdminController(GuestImportService guestImportService, TwilioNotificationService twilioNotificationService, GuestRepository guestRepository) {
        this.guestImportService = guestImportService;
        this.twilioNotificationService = twilioNotificationService;
        this.guestRepository = guestRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío"));
        }
        
        try {
            var importedGuests = guestImportService.importGuestsFromCsv(file);
            var resultList = importedGuests.stream().map(g -> Map.of(
                "nombre", g.getNombreInvitado(),
                "codigoInvitacion", g.getCodigoInvitacion(),
                "urlDirecta", "/invitacion/" + g.getToken()
            )).toList();

            return ResponseEntity.ok(Map.of(
                "message", "Importación exitosa",
                "registros_insertados", importedGuests.size(),
                "invitados", resultList
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Ocurrió un error procesando el CSV",
                "details", e.getMessage()
            ));
        }
    }

    @PostMapping("/send-invitations")
    public ResponseEntity<?> sendInvitations() {
        List<Guest> pendientes = guestRepository.findAll().stream()
            .filter(g -> g.getEstatus() == com.tlamatinisoft.mosentiliztli.model.GuestStatus.PENDIENTE)
            .toList();
            
        int enviados = 0;
        int fallidos = 0;
        
        for (Guest guest : pendientes) {
            try {
                twilioNotificationService.sendInvitation(guest);
                enviados++;
            } catch (Exception e) {
                fallidos++;
                System.err.println("Error al enviar WhatsApp a " + guest.getNombreInvitado() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Proceso de envío finalizado",
            "enviados", enviados,
            "fallidos", fallidos
        ));
    }
}
