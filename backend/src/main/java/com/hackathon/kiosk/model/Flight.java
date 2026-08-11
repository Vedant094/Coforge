package com.hackathon.kiosk.model;

public class Flight {
    private Long id;
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private String departureTime; // ISO string, kept simple for JSON demo data
    private String gate;
    private String terminal;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }
    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
