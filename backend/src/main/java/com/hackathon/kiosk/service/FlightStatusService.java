package com.hackathon.kiosk.service;

import com.hackathon.kiosk.model.Flight;
import com.hackathon.kiosk.store.MockDataStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FlightStatusService {

    private final MockDataStore store;

    public FlightStatusService(MockDataStore store) {
        this.store = store;
    }

    public Map<String, Object> getStatus(String flightNumber) {
        Map<String, Object> result = new HashMap<>();
        Optional<Flight> flightOpt = store.findFlightByNumber(flightNumber);
        if (flightOpt.isEmpty()) {
            result.put("status", "NOT_FOUND");
            return result;
        }
        Flight f = flightOpt.get();
        result.put("status", "OK");
        result.put("flightNumber", f.getFlightNumber());
        result.put("airline", f.getAirline());
        result.put("origin", f.getOrigin());
        result.put("destination", f.getDestination());
        result.put("departureTime", f.getDepartureTime());
        result.put("gate", f.getGate());
        result.put("terminal", f.getTerminal());
        result.put("flightStatus", f.getStatus());
        return result;
    }
}
