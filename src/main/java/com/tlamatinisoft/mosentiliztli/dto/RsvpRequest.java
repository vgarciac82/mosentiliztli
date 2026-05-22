package com.tlamatinisoft.mosentiliztli.dto;

import com.tlamatinisoft.mosentiliztli.model.GuestStatus;
import jakarta.validation.constraints.NotNull;

public class RsvpRequest {
    @NotNull(message = "El token es obligatorio")
    private String token;
    @NotNull(message = "El estado de decisión es obligatorio")
    private GuestStatus statusDecision;
    @NotNull(message = "La cantidad de pases confirmados es obligatoria")
    private Integer pasesConfirmados;

    public RsvpRequest() {}
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public GuestStatus getStatusDecision() { return statusDecision; }
    public void setStatusDecision(GuestStatus statusDecision) { this.statusDecision = statusDecision; }
    public Integer getPasesConfirmados() { return pasesConfirmados; }
    public void setPasesConfirmados(Integer pasesConfirmados) { this.pasesConfirmados = pasesConfirmados; }
}
