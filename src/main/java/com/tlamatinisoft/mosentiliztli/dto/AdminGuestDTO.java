package com.tlamatinisoft.mosentiliztli.dto;

import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestStatus;

public class AdminGuestDTO {
    private String nombre;
    private String familia;
    private Integer pasesAsignados;
    private Integer pasesConfirmados;
    private GuestStatus estatus;
    private String notas;
    private String codigoInvitacion;
    private String celular;
    private java.util.UUID id;

    public AdminGuestDTO(Guest guest, Integer pasesConfirmados) {
        this.id = guest.getId();
        this.nombre = guest.getNombreInvitado();
        this.familia = guest.getFamilia();
        this.pasesAsignados = guest.getPasesAsignados();
        this.pasesConfirmados = pasesConfirmados != null ? pasesConfirmados : 0;
        this.estatus = guest.getEstatus();
        this.notas = guest.getNotas();
        this.codigoInvitacion = guest.getCodigoInvitacion();
        this.celular = guest.getCelular();
    }

    public String getNombre() { return nombre; }
    public String getFamilia() { return familia; }
    public Integer getPasesAsignados() { return pasesAsignados; }
    public Integer getPasesConfirmados() { return pasesConfirmados; }
    public GuestStatus getEstatus() { return estatus; }
    public String getNotas() { return notas; }
    public String getCodigoInvitacion() { return codigoInvitacion; }
    public String getCelular() { return celular; }
    public java.util.UUID getId() { return id; }
}
