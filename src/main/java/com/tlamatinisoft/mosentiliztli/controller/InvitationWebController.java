package com.tlamatinisoft.mosentiliztli.controller;

import com.tlamatinisoft.mosentiliztli.dto.GuestResponse;
import com.tlamatinisoft.mosentiliztli.dto.RsvpRequest;
import com.tlamatinisoft.mosentiliztli.exception.ResourceNotFoundException;
import com.tlamatinisoft.mosentiliztli.exception.RsvpValidationException;
import com.tlamatinisoft.mosentiliztli.service.GuestService;
import com.tlamatinisoft.mosentiliztli.service.RsvpService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/invitacion")
public class InvitationWebController {

    private final GuestService guestService;
    private final RsvpService rsvpService;

    public InvitationWebController(GuestService guestService, RsvpService rsvpService) {
        this.guestService = guestService;
        this.rsvpService = rsvpService;
    }

    @GetMapping
    public String showLogin(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Código de invitación no válido o no encontrado.");
        }
        return "login";
    }

    @PostMapping
    public String processLogin(@RequestParam String codigoInvitacion) {
        try {
            GuestResponse guest = guestService.getGuestByCodigo(codigoInvitacion);
            return "redirect:/invitacion/" + guest.getToken();
        } catch (ResourceNotFoundException e) {
            return "redirect:/invitacion?error=true";
        }
    }

    @GetMapping("/{token}")
    public String showInvitation(@PathVariable String token, 
                                 @RequestParam(required = false) String success,
                                 @RequestParam(required = false) String error,
                                 Model model) {
        try {
            GuestResponse guest = guestService.getGuestByToken(token);
            model.addAttribute("guest", guest);
            if (success != null) model.addAttribute("success", true);
            if (error != null) model.addAttribute("error", error);
            return "invitation";
        } catch (ResourceNotFoundException e) {
            return "redirect:/invitacion?error=true";
        }
    }

    @PostMapping("/{token}/rsvp")
    public String processRsvp(@PathVariable String token, 
                              @ModelAttribute RsvpRequest rsvpRequest, 
                              RedirectAttributes redirectAttributes) {
        try {
            rsvpRequest.setToken(token);
            rsvpService.processRsvp(rsvpRequest);
            redirectAttributes.addAttribute("success", "true");
        } catch (RsvpValidationException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Ocurrió un error al procesar tu respuesta.");
        }
        return "redirect:/invitacion/" + token;
    }
}
