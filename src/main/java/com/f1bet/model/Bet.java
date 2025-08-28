package com.f1bet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "bets")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String eventId;
    private Integer driverId;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    private Integer odds;
    private String status;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAwarded;

    public Bet(Long userId, String eventId, Integer driverId, BigDecimal amount, Integer odds, String status) {
        this.userId = userId;
        this.eventId = eventId;
        this.driverId = driverId;
        this.amount = amount;
        this.odds = odds;
        this.status = status;
    }

    public Bet() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getOdds() {
        return odds;
    }

    public void setOdds(Integer odds) {
        this.odds = odds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAwarded() {
        return totalAwarded;
    }

    public void setTotalAwarded(BigDecimal totalAwarded) {
        this.totalAwarded = totalAwarded;
    }
}