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
public class InvitationWebController {

    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/invitacion";
    }

    private final GuestService guestService;
    private final RsvpService rsvpService;
    private final com.tlamatinisoft.mosentiliztli.service.GuestVisitService guestVisitService;

    public InvitationWebController(GuestService guestService, RsvpService rsvpService, com.tlamatinisoft.mosentiliztli.service.GuestVisitService guestVisitService) {
        this.guestService = guestService;
        this.rsvpService = rsvpService;
        this.guestVisitService = guestVisitService;
    }

    @GetMapping("/invitacion")
    public String showLogin(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Código de invitación no válido o no encontrado.");
        }
        return "login";
    }

    @PostMapping("/invitacion")
    public String processLogin(@RequestParam String codigoInvitacion) {
        try {
            GuestResponse guest = guestService.getGuestByCodigo(codigoInvitacion);
            return "redirect:/invitacion/" + guest.getToken();
        } catch (ResourceNotFoundException e) {
            return "redirect:/invitacion?error=true";
        }
    }

    @GetMapping("/invitacion/{token}")
    public String showInvitation(@PathVariable String token, 
                                 @RequestParam(required = false) String success,
                                 @RequestParam(required = false) String error,
                                 jakarta.servlet.http.HttpServletRequest request,
                                 Model model) {
        try {
            GuestResponse guest = guestService.getGuestByToken(token);
            
            // Log the visit asynchronously or simply inline
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();
            guestVisitService.logVisit(token, userAgent, ipAddress);

            model.addAttribute("guest", guest);
            if (success != null) model.addAttribute("success", true);
            if (error != null) model.addAttribute("error", error);
            return "invitation";
        } catch (ResourceNotFoundException e) {
            return "redirect:/invitacion?error=true";
        }
    }

    @PostMapping("/invitacion/{token}/rsvp")
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
