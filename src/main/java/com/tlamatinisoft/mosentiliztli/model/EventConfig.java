package com.tlamatinisoft.mosentiliztli.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_config")
public class EventConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "total_capacity")
    private Integer totalCapacity;

    public EventConfig() {}
    public EventConfig(Long id, LocalDateTime expirationDate, Integer totalCapacity) {
        this.id = id; this.expirationDate = expirationDate; this.totalCapacity = totalCapacity;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { this.expirationDate = expirationDate; }
    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }
}
