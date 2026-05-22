package com.tlamatinisoft.mosentiliztli.dto;

import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestStatus;

import java.util.UUID;

public class GuestResponse {
    private UUID id;
    private String familia;
    private Integer pasesAsignados;
    private String nombreInvitado;
    private String grupo;
    private String token;
    private GuestStatus estatus;

    public GuestResponse() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFamilia() { return familia; }
    public void setFamilia(String familia) { this.familia = familia; }
    public Integer getPasesAsignados() { return pasesAsignados; }
    public void setPasesAsignados(Integer pasesAsignados) { this.pasesAsignados = pasesAsignados; }
    public String getNombreInvitado() { return nombreInvitado; }
    public void setNombreInvitado(String nombreInvitado) { this.nombreInvitado = nombreInvitado; }
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public GuestStatus getEstatus() { return estatus; }
    public void setEstatus(GuestStatus estatus) { this.estatus = estatus; }

    public static GuestResponse fromEntity(Guest guest) {
        GuestResponse response = new GuestResponse();
        response.setId(guest.getId());
        response.setFamilia(guest.getFamilia());
        response.setPasesAsignados(guest.getPasesAsignados());
        response.setNombreInvitado(guest.getNombreInvitado());
        response.setGrupo(guest.getGrupo());
        response.setToken(guest.getToken());
        response.setEstatus(guest.getEstatus());
        return response;
    }
}
