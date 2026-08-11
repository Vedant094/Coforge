package com.hackathon.kiosk.model;

import java.math.BigDecimal;

public class Seat {
    private Long id;
    private Long flightId;
    private String seatNumber;
    private String seatClass;
    private boolean available;
    private BigDecimal upgradePrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getSeatClass() { return seatClass; }
    public void setSeatClass(String seatClass) { this.seatClass = seatClass; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public BigDecimal getUpgradePrice() { return upgradePrice; }
    public void setUpgradePrice(BigDecimal upgradePrice) { this.upgradePrice = upgradePrice; }
}
