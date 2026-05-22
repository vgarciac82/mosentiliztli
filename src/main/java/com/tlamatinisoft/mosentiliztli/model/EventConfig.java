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

    @Column(name = "twilio_account_sid")
    private String twilioAccountSid;

    @Column(name = "twilio_auth_token")
    private String twilioAuthToken;

    @Column(name = "twilio_phone_number")
    private String twilioPhoneNumber;

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
    public String getTwilioAccountSid() { return twilioAccountSid; }
    public void setTwilioAccountSid(String twilioAccountSid) { this.twilioAccountSid = twilioAccountSid; }
    public String getTwilioAuthToken() { return twilioAuthToken; }
    public void setTwilioAuthToken(String twilioAuthToken) { this.twilioAuthToken = twilioAuthToken; }
    public String getTwilioPhoneNumber() { return twilioPhoneNumber; }
    public void setTwilioPhoneNumber(String twilioPhoneNumber) { this.twilioPhoneNumber = twilioPhoneNumber; }
}
