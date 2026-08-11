package com.hackathon.kiosk.controller;

import com.hackathon.kiosk.model.Flight;
import com.hackathon.kiosk.store.MockDataStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Simple read endpoint for testing/demo purposes without going through chat. */
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final MockDataStore store;

    public FlightController(MockDataStore store) {
        this.store = store;
    }

    @GetMapping
    public List<Flight> allFlights() {
        return store.allFlights();
    }
}
