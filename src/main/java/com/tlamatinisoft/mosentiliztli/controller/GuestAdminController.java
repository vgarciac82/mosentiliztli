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
                guest.setEstatus(com.tlamatinisoft.mosentiliztli.model.GuestStatus.INVITACION_ENVIADA);
                guestRepository.save(guest);
                enviados++;
            } catch (Exception e) {
                fallidos++;
                guest.setEstatus(com.tlamatinisoft.mosentiliztli.model.GuestStatus.ERROR_ENVIO);
                guestRepository.save(guest);
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

    @PutMapping("/{id}/phone")
    public ResponseEntity<?> updateGuestPhone(@PathVariable java.util.UUID id, @RequestBody Map<String, String> payload) {
        var guestOpt = guestRepository.findById(id);
        if (guestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invitado no encontrado"));
        }
        
        Guest guest = guestOpt.get();
        String celularRaw = payload.get("celular");
        if (celularRaw == null || celularRaw.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Celular inválido o vacío"));
        }
        
        // Normalizar celular
        String celular = celularRaw.replaceAll("[^0-9+]", "");
        if (celular.length() == 10 && !celular.startsWith("+")) {
            celular = "+52" + celular;
        } else if (celular.startsWith("52") && celular.length() == 12) {
            celular = "+" + celular;
        } else if (!celular.startsWith("+") && !celular.isEmpty()) {
            celular = "+" + celular;
        }
        
        // Verificar duplicados solo si es diferente al actual
        if (!celular.equals(guest.getCelular()) && guestRepository.findByCelular(celular).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El celular ya está registrado a otro invitado"));
        }
        
        guest.setCelular(celular);
        guest.setEstatus(com.tlamatinisoft.mosentiliztli.model.GuestStatus.PENDIENTE);
        guestRepository.save(guest);
        
        return ResponseEntity.ok(Map.of(
            "message", "Celular actualizado y estatus reiniciado a PENDIENTE",
            "nuevo_celular", guest.getCelular()
        ));
    }

    @PostMapping
    public ResponseEntity<?> createGuest(@RequestBody Map<String, Object> payload) {
        try {
            String nombre = (String) payload.get("nombreInvitado");
            String celular = (String) payload.get("celular");
            int pases = Integer.parseInt(payload.getOrDefault("pasesAsignados", "1").toString());
            String familia = (String) payload.get("familia");
            String grupo = (String) payload.get("grupo");

            Guest newGuest = guestImportService.createSingleGuest(nombre, celular, pases, familia, grupo);

            return ResponseEntity.ok(Map.of(
                "message", "Invitado registrado exitosamente",
                "guest_id", newGuest.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno al crear invitado"));
        }
    }

    @PutMapping("/{id}/reset-status")
    public ResponseEntity<?> resetGuestStatus(@PathVariable java.util.UUID id) {
        var guestOpt = guestRepository.findById(id);
        if (guestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invitado no encontrado"));
        }
        
        Guest guest = guestOpt.get();
        guest.setEstatus(com.tlamatinisoft.mosentiliztli.model.GuestStatus.PENDIENTE);
        guestRepository.save(guest);
        
        return ResponseEntity.ok(Map.of(
            "message", "Estatus reiniciado a PENDIENTE exitosamente"
        ));
    }
}
