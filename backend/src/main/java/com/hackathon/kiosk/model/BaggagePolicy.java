package com.hackathon.kiosk.model;

import java.math.BigDecimal;

public class BaggagePolicy {
    private Long id;
    private String airline;
    private String classType;
    private int freeBagCount;
    private int freeWeightKg;
    private BigDecimal extraBagFee;
    private BigDecimal overweightFee;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }
    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }
    public int getFreeBagCount() { return freeBagCount; }
    public void setFreeBagCount(int freeBagCount) { this.freeBagCount = freeBagCount; }
    public int getFreeWeightKg() { return freeWeightKg; }
    public void setFreeWeightKg(int freeWeightKg) { this.freeWeightKg = freeWeightKg; }
    public BigDecimal getExtraBagFee() { return extraBagFee; }
    public void setExtraBagFee(BigDecimal extraBagFee) { this.extraBagFee = extraBagFee; }
    public BigDecimal getOverweightFee() { return overweightFee; }
    public void setOverweightFee(BigDecimal overweightFee) { this.overweightFee = overweightFee; }
}
