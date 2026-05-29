package com.tlamatinisoft.mosentiliztli.service;

import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestVisit;
import com.tlamatinisoft.mosentiliztli.repository.GuestRepository;
import com.tlamatinisoft.mosentiliztli.repository.GuestVisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GuestVisitService {

    private final GuestVisitRepository guestVisitRepository;
    private final GuestRepository guestRepository;

    public GuestVisitService(GuestVisitRepository guestVisitRepository, GuestRepository guestRepository) {
        this.guestVisitRepository = guestVisitRepository;
        this.guestRepository = guestRepository;
    }

    @Transactional
    public void logVisit(String token, String userAgent, String ipAddress) {
        guestRepository.findByToken(token).ifPresent(guest -> {
            GuestVisit visit = new GuestVisit(guest, LocalDateTime.now(), userAgent, ipAddress);
            guestVisitRepository.save(visit);
        });
    }
}
