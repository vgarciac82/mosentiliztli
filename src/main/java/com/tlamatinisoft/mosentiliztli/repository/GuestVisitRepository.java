package com.tlamatinisoft.mosentiliztli.repository;

import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestVisitRepository extends JpaRepository<GuestVisit, Long> {
    List<GuestVisit> findByGuestOrderByVisitedAtDesc(Guest guest);
    long countByGuest(Guest guest);
}
