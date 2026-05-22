package com.tlamatinisoft.mosentiliztli.controller;

import com.tlamatinisoft.mosentiliztli.dto.AdminGuestDTO;
import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestStatus;
import com.tlamatinisoft.mosentiliztli.model.RsvpResponse;
import com.tlamatinisoft.mosentiliztli.repository.GuestRepository;
import com.tlamatinisoft.mosentiliztli.repository.RsvpResponseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminWebController {

    private final GuestRepository guestRepository;
    private final RsvpResponseRepository rsvpResponseRepository;

    public AdminWebController(GuestRepository guestRepository, RsvpResponseRepository rsvpResponseRepository) {
        this.guestRepository = guestRepository;
        this.rsvpResponseRepository = rsvpResponseRepository;
    }

    @GetMapping
    public String showDashboard(Model model) {
        List<Guest> guests = guestRepository.findAll();
        List<RsvpResponse> rsvps = rsvpResponseRepository.findAll();
        
        Map<UUID, Integer> rsvpMap = rsvps.stream()
            .collect(Collectors.toMap(r -> r.getGuest().getId(), RsvpResponse::getPasesConfirmados, (v1, v2) -> v1)); // merge function in case of duplicates
            
        List<AdminGuestDTO> dtoList = guests.stream()
            .map(g -> new AdminGuestDTO(g, rsvpMap.get(g.getId())))
            .collect(Collectors.toList());
            
        long totalInvitados = guests.size();
        long totalPasesAsignados = guests.stream().mapToLong(Guest::getPasesAsignados).sum();
        long totalPasesConfirmados = rsvps.stream().mapToLong(RsvpResponse::getPasesConfirmados).sum();
        
        long totalFamiliasConfirmadas = guests.stream().filter(g -> g.getEstatus() == GuestStatus.CONFIRMADO).count();
        long totalFamiliasDeclinadas = guests.stream().filter(g -> g.getEstatus() == GuestStatus.DECLINADO).count();
        long totalFamiliasPendientes = guests.stream().filter(g -> g.getEstatus() == GuestStatus.PENDIENTE).count();

        model.addAttribute("invitados", dtoList);
        model.addAttribute("totalInvitados", totalInvitados);
        model.addAttribute("totalPasesAsignados", totalPasesAsignados);
        model.addAttribute("totalPasesConfirmados", totalPasesConfirmados);
        model.addAttribute("totalFamiliasConfirmadas", totalFamiliasConfirmadas);
        model.addAttribute("totalFamiliasDeclinadas", totalFamiliasDeclinadas);
        model.addAttribute("totalFamiliasPendientes", totalFamiliasPendientes);

        return "admin/dashboard";
    }
}
