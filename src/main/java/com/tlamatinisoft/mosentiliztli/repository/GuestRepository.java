package com.tlamatinisoft.mosentiliztli.repository;

import com.tlamatinisoft.mosentiliztli.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID> {
    Optional<Guest> findByToken(String token);
    Optional<Guest> findByCodigoInvitacion(String codigoInvitacion);
    Optional<Guest> findByCelular(String celular);
}
