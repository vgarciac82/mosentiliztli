package com.tlamatinisoft.mosentiliztli.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rsvp_response")
public class RsvpResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;
    @Column(name = "pases_confirmados", nullable = false)
    private Integer pasesConfirmados;
    @Column(name = "fecha_confirmacion", nullable = false)
    private LocalDateTime fechaConfirmacion;

    public RsvpResponse() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public Integer getPasesConfirmados() { return pasesConfirmados; }
    public void setPasesConfirmados(Integer pasesConfirmados) { this.pasesConfirmados = pasesConfirmados; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }
}
