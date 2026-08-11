package com.hackathon.kiosk.model;

import java.util.List;

/** Maps directly to the structure of mock-data.json. */
public class MockData {
    private List<Flight> flights;
    private List<Passenger> passengers;
    private List<Booking> bookings;
    private List<Seat> seats;
    private List<BaggagePolicy> baggagePolicies;

    public List<Flight> getFlights() { return flights; }
    public void setFlights(List<Flight> flights) { this.flights = flights; }
    public List<Passenger> getPassengers() { return passengers; }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
    public List<BaggagePolicy> getBaggagePolicies() { return baggagePolicies; }
    public void setBaggagePolicies(List<BaggagePolicy> baggagePolicies) { this.baggagePolicies = baggagePolicies; }
}
