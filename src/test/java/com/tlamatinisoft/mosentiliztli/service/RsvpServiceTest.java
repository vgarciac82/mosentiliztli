package com.tlamatinisoft.mosentiliztli.service;

import com.tlamatinisoft.mosentiliztli.dto.RsvpRequest;
import com.tlamatinisoft.mosentiliztli.exception.RsvpValidationException;
import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestStatus;
import com.tlamatinisoft.mosentiliztli.repository.GuestRepository;
import com.tlamatinisoft.mosentiliztli.repository.RsvpResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RsvpServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private RsvpResponseRepository rsvpResponseRepository;

    @InjectMocks
    private RsvpService rsvpService;

    private Guest testGuest;
    private RsvpRequest testRequest;

    @BeforeEach
    void setUp() {
        testGuest = new Guest();
        testGuest.setToken("valid-token");
        testGuest.setPasesAsignados(2);
        testGuest.setEstatus(GuestStatus.PENDIENTE);

        testRequest = new RsvpRequest();
        testRequest.setToken("valid-token");
        testRequest.setStatusDecision(GuestStatus.CONFIRMADO);
        testRequest.setPasesConfirmados(2);
    }

    @Test
    void testProcessRsvp_Success() {
        when(guestRepository.findByToken("valid-token")).thenReturn(Optional.of(testGuest));

        rsvpService.processRsvp(testRequest);

        verify(guestRepository, times(1)).save(testGuest);
        verify(rsvpResponseRepository, times(1)).save(any());
    }

    @Test
    void testProcessRsvp_ExceedsLimit_ThrowsException() {
        testRequest.setPasesConfirmados(3); // Mayor que pasesAsignados (2)
        when(guestRepository.findByToken("valid-token")).thenReturn(Optional.of(testGuest));

        assertThrows(RsvpValidationException.class, () -> rsvpService.processRsvp(testRequest));
        verify(guestRepository, never()).save(any());
    }

    @Test
    void testProcessRsvp_LockedWhenDeclined_ThrowsException() {
        testGuest.setEstatus(GuestStatus.DECLINADO);
        when(guestRepository.findByToken("valid-token")).thenReturn(Optional.of(testGuest));

        assertThrows(RsvpValidationException.class, () -> rsvpService.processRsvp(testRequest));
        verify(guestRepository, never()).save(any());
    }
}
