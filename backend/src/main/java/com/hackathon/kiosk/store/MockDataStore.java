package com.hackathon.kiosk.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.kiosk.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Loads mock-data.json once at startup and holds it in memory for the rest
 * of the app's lifetime. No database involved - this is the single source
 * of truth for the demo. Objects returned are live references into the
 * loaded lists, so services can mutate them directly (e.g. mark a booking
 * checked in) without a separate "save" step. Changes are in-memory only
 * and reset when the backend restarts, same as the H2 approach before it.
 *
 * To add more test data for a demo, just edit mock-data.json - no schema
 * migration, no SQL.
 */
@Component
public class MockDataStore {

    private MockData data;

    @PostConstruct
    public void load() {
        try (InputStream is = new ClassPathResource("mock-data.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            this.data = mapper.readValue(is, MockData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load mock-data.json", e);
        }
    }

    public List<Flight> allFlights() {
        return data.getFlights();
    }

    public Optional<Flight> findFlightByNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) return Optional.empty();
        return data.getFlights().stream()
                .filter(f -> f.getFlightNumber().equalsIgnoreCase(flightNumber.trim()))
                .findFirst();
    }

    public Optional<Flight> findFlightById(Long id) {
        return data.getFlights().stream().filter(f -> f.getId().equals(id)).findFirst();
    }

    public Optional<Booking> findBookingByPnr(String pnr) {
        if (pnr == null || pnr.isBlank()) return Optional.empty();
        return data.getBookings().stream()
                .filter(b -> b.getPnr().equalsIgnoreCase(pnr.trim()))
                .findFirst();
    }

    public List<Seat> findAvailableSeatsByFlightId(Long flightId) {
        return data.getSeats().stream()
                .filter(s -> s.getFlightId().equals(flightId) && s.isAvailable())
                .toList();
    }

    public Optional<Seat> findSeatByFlightAndNumber(Long flightId, String seatNumber) {
        return data.getSeats().stream()
                .filter(s -> s.getFlightId().equals(flightId) && s.getSeatNumber().equalsIgnoreCase(seatNumber))
                .findFirst();
    }

    public Optional<BaggagePolicy> findBaggagePolicy(String airline, String classType) {
        if (airline == null || airline.isBlank()) return Optional.empty();
        String cls = (classType == null || classType.isBlank()) ? "ECONOMY" : classType.toUpperCase();
        return data.getBaggagePolicies().stream()
                .filter(p -> p.getAirline().equalsIgnoreCase(airline.trim()) && p.getClassType().equalsIgnoreCase(cls))
                .findFirst();
    }

    public Optional<Passenger> findPassengerById(Long id) {
        return data.getPassengers().stream().filter(p -> p.getId().equals(id)).findFirst();
    }
}
