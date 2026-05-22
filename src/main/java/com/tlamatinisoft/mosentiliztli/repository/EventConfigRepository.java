package com.tlamatinisoft.mosentiliztli.repository;

import com.tlamatinisoft.mosentiliztli.model.EventConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventConfigRepository extends JpaRepository<EventConfig, Long> {
}
