package com.tlamatinisoft.mosentiliztli.repository;

import com.tlamatinisoft.mosentiliztli.model.RsvpResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RsvpResponseRepository extends JpaRepository<RsvpResponse, Long> {
}
