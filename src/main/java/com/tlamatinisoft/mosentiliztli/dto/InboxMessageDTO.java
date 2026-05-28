package com.tlamatinisoft.mosentiliztli.dto;

import com.tlamatinisoft.mosentiliztli.model.GuestStatus;
import com.tlamatinisoft.mosentiliztli.model.TwilioMessage;

import java.time.LocalDateTime;

public class InboxMessageDTO {
    private String nombreRemitente;
    private String celular;
    private String mensaje;
    private LocalDateTime fechaRecepcion;
    private GuestStatus estatusInvitado;

    public InboxMessageDTO(TwilioMessage message) {
        if (message.getGuest() != null) {
            this.nombreRemitente = message.getGuest().getNombreInvitado() + " (" + message.getGuest().getFamilia() + ")";
            this.estatusInvitado = message.getGuest().getEstatus();
        } else {
            this.nombreRemitente = "Desconocido";
            this.estatusInvitado = null;
        }
        this.celular = message.getNumeroRemitente();
        this.mensaje = message.getCuerpoMensaje();
        this.fechaRecepcion = message.getFechaRecepcion();
    }

    public String getNombreRemitente() { return nombreRemitente; }
    public String getCelular() { return celular; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public GuestStatus getEstatusInvitado() { return estatusInvitado; }
}
