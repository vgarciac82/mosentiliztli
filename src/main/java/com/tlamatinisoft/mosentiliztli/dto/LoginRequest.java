package com.tlamatinisoft.mosentiliztli.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "El código de invitación es obligatorio")
    private String codigoInvitacion;

    public LoginRequest() {}
    public String getCodigoInvitacion() { return codigoInvitacion; }
    public void setCodigoInvitacion(String codigoInvitacion) { this.codigoInvitacion = codigoInvitacion; }
}
