package com.hackathon.kiosk.service;

import com.hackathon.kiosk.model.Booking;
import com.hackathon.kiosk.model.Flight;
import com.hackathon.kiosk.store.MockDataStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CheckInService {

    private final MockDataStore store;

    public CheckInService(MockDataStore store) {
        this.store = store;
    }

    public Map<String, Object> checkIn(String pnr) {
        Map<String, Object> result = new HashMap<>();
        if (pnr == null || pnr.isBlank()) {
            result.put("status", "NEEDS_PNR");
            return result;
        }

        Optional<Booking> bookingOpt = store.findBookingByPnr(pnr);
        if (bookingOpt.isEmpty()) {
            result.put("status", "NOT_FOUND");
            return result;
        }

        Booking booking = bookingOpt.get(); // live reference into the in-memory list
        Optional<Flight> flightOpt = store.findFlightById(booking.getFlightId());

        if (booking.isCheckedIn()) {
            result.put("status", "ALREADY_CHECKED_IN");
            result.put("seat", booking.getSeatNumber());
        } else {
            booking.setCheckedIn(true);
            booking.setBoardingPassIssued(true);
            if (booking.getSeatNumber() == null) {
                booking.setSeatNumber("20A"); // default economy seat for demo
            }
            result.put("status", "CHECKED_IN");
            result.put("seat", booking.getSeatNumber());
        }

        flightOpt.ifPresent(flight -> {
            result.put("flightNumber", flight.getFlightNumber());
            result.put("airline", flight.getAirline());
            result.put("gate", flight.getGate());
            result.put("terminal", flight.getTerminal());
            result.put("departureTime", flight.getDepartureTime());
        });
        result.put("pnr", booking.getPnr());
        return result;
    }
}
