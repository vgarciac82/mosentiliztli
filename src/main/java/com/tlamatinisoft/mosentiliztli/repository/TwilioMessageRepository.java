package com.tlamatinisoft.mosentiliztli.repository;

import com.tlamatinisoft.mosentiliztli.model.TwilioMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TwilioMessageRepository extends JpaRepository<TwilioMessage, Long> {
    List<TwilioMessage> findAllByOrderByFechaRecepcionDesc();
}
