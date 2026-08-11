package com.hackathon.kiosk.service;

import com.hackathon.kiosk.model.Booking;
import com.hackathon.kiosk.model.Seat;
import com.hackathon.kiosk.store.MockDataStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final MockDataStore store;

    public SeatService(MockDataStore store) {
        this.store = store;
    }

    public Map<String, Object> getAvailableSeats(Long flightId) {
        List<Seat> seats = store.findAvailableSeatsByFlightId(flightId);
        Map<String, Object> result = new HashMap<>();
        result.put("availableSeats", seats.stream().map(s -> Map.of(
                "seatNumber", s.getSeatNumber(),
                "class", s.getSeatClass(),
                "upgradePrice", s.getUpgradePrice()
        )).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> selectSeat(String pnr, String seatNumber) {
        Map<String, Object> result = new HashMap<>();
        Optional<Booking> bookingOpt = store.findBookingByPnr(pnr);
        if (bookingOpt.isEmpty()) {
            result.put("status", "NOT_FOUND");
            return result;
        }
        Booking booking = bookingOpt.get();
        Optional<Seat> seatOpt = store.findSeatByFlightAndNumber(booking.getFlightId(), seatNumber);

        if (seatOpt.isEmpty() || !seatOpt.get().isAvailable()) {
            result.put("status", "SEAT_UNAVAILABLE");
            return result;
        }

        Seat seat = seatOpt.get();
        seat.setAvailable(false);
        booking.setSeatNumber(seat.getSeatNumber());

        result.put("status", "SEAT_CONFIRMED");
        result.put("seatNumber", seat.getSeatNumber());
        result.put("seatClass", seat.getSeatClass());
        result.put("upgradePrice", seat.getUpgradePrice());
        return result;
    }
}
