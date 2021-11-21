package com.upt.cti.smartwallet.model;

public class Payment {
    public String timestamp;
    private double cost;
    private String name;
    private String type;

    public Payment(String timestamp, double cost, String name, String type) {
        this.timestamp = timestamp;
        this.cost = cost;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public String getType() {
        return type;
    }
}
