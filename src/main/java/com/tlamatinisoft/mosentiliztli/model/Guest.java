package com.tlamatinisoft.mosentiliztli.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "guest")
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "guest_id", updatable = false, nullable = false)
    private UUID id;
    private String familia;
    @Column(name = "pases_asignados", nullable = false)
    private Integer pasesAsignados;
    private String celular;
    @Column(name = "nombre_invitado")
    private String nombreInvitado;
    private String grupo;
    @Column(columnDefinition = "TEXT")
    private String notas;
    @Column(unique = true, nullable = false)
    private String token;
    @Column(name = "codigo_invitacion", unique = true, nullable = false)
    private String codigoInvitacion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuestStatus estatus = GuestStatus.PENDIENTE;

    public Guest() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFamilia() { return familia; }
    public void setFamilia(String familia) { this.familia = familia; }
    public Integer getPasesAsignados() { return pasesAsignados; }
    public void setPasesAsignados(Integer pasesAsignados) { this.pasesAsignados = pasesAsignados; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getNombreInvitado() { return nombreInvitado; }
    public void setNombreInvitado(String nombreInvitado) { this.nombreInvitado = nombreInvitado; }
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getCodigoInvitacion() { return codigoInvitacion; }
    public void setCodigoInvitacion(String codigoInvitacion) { this.codigoInvitacion = codigoInvitacion; }
    public GuestStatus getEstatus() { return estatus; }
    public void setEstatus(GuestStatus estatus) { this.estatus = estatus; }
}
