package com.hackathon.kiosk.service;

import com.hackathon.kiosk.model.BaggagePolicy;
import com.hackathon.kiosk.store.MockDataStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BaggageService {

    private final MockDataStore store;

    public BaggageService(MockDataStore store) {
        this.store = store;
    }

    public Map<String, Object> getPolicy(String airline, String classType) {
        Map<String, Object> result = new HashMap<>();
        Optional<BaggagePolicy> policyOpt = store.findBaggagePolicy(airline, classType);

        if (policyOpt.isEmpty()) {
            result.put("status", "NOT_FOUND");
            return result;
        }

        BaggagePolicy p = policyOpt.get();
        result.put("status", "OK");
        result.put("airline", p.getAirline());
        result.put("classType", p.getClassType());
        result.put("freeBagCount", p.getFreeBagCount());
        result.put("freeWeightKg", p.getFreeWeightKg());
        result.put("extraBagFee", p.getExtraBagFee());
        result.put("overweightFee", p.getOverweightFee());
        return result;
    }

    public Map<String, Object> calculateExtraFee(String airline, String classType, int bagCount) {
        Map<String, Object> policy = getPolicy(airline, classType);
        if (!"OK".equals(policy.get("status"))) {
            return policy;
        }
        int freeBags = (int) policy.get("freeBagCount");
        int extraBags = Math.max(0, bagCount - freeBags);
        BigDecimal fee = ((BigDecimal) policy.get("extraBagFee")).multiply(BigDecimal.valueOf(extraBags));

        Map<String, Object> result = new HashMap<>(policy);
        result.put("requestedBags", bagCount);
        result.put("extraBags", extraBags);
        result.put("totalExtraFee", fee);
        return result;
    }
}
