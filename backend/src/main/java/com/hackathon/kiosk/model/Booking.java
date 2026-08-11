package com.hackathon.kiosk.model;

public class Booking {
    private Long id;
    private String pnr;
    private Long passengerId;
    private Long flightId;
    private boolean checkedIn;
    private String seatNumber;
    private int bagCount;
    private boolean boardingPassIssued;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public int getBagCount() { return bagCount; }
    public void setBagCount(int bagCount) { this.bagCount = bagCount; }
    public boolean isBoardingPassIssued() { return boardingPassIssued; }
    public void setBoardingPassIssued(boolean boardingPassIssued) { this.boardingPassIssued = boardingPassIssued; }
}
