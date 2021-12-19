package com.upt.cti.smartwallet.model;

import java.io.Serializable;

public class Payment implements Serializable {
    public String timestamp;
    private double cost;
    private String name;
    private String type;
    private String user;

    public Payment() { }

    public Payment(String timestamp, double cost, String name, String type, String user) {
        this.timestamp = timestamp;
        this.cost = cost;
        this.name = name;
        this.type = type;
        this.user = user;
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public void setUser(String user){ this.user = user;}

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public String getType() {
        return type;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public String gertUser(){
        return user;
    }

    public Payment copy(){
        Payment copyOfPayment = new Payment(this.timestamp, this.cost, this.name, this.type, this.user);
        return copyOfPayment;
    }
}
