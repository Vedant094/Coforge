package com.hackathon.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Entities {
    @JsonProperty("flight_number")
    private String flightNumber = "";
    private String pnr = "";
    @JsonProperty("passenger_name")
    private String passengerName = "";
    @JsonProperty("seat_preference")
    private String seatPreference = "";
    @JsonProperty("bag_count")
    private String bagCount = "";
    private String destination = "";

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public String getSeatPreference() { return seatPreference; }
    public void setSeatPreference(String seatPreference) { this.seatPreference = seatPreference; }
    public String getBagCount() { return bagCount; }
    public void setBagCount(String bagCount) { this.bagCount = bagCount; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
}
