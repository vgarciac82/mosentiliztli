package com.tlamatinisoft.mosentiliztli.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "twilio_message")
public class TwilioMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "numero_remitente", nullable = false)
    private String numeroRemitente;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Guest guest;
    @Column(name = "cuerpo_mensaje", columnDefinition = "TEXT")
    private String cuerpoMensaje;
    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    public TwilioMessage() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroRemitente() { return numeroRemitente; }
    public void setNumeroRemitente(String numeroRemitente) { this.numeroRemitente = numeroRemitente; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public String getCuerpoMensaje() { return cuerpoMensaje; }
    public void setCuerpoMensaje(String cuerpoMensaje) { this.cuerpoMensaje = cuerpoMensaje; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }
}
