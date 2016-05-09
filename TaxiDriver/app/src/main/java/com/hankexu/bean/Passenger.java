package com.hankexu.bean;

/**
 * Created by hanke on 2016-01-10.
 */
public class Passenger {
    private String inception;
    private String destination;
    private String name;
    private String phone;
    private String role = "passenger";


    public Passenger(String inception, String destination, String name, String phone) {
        this.inception = inception;
        this.destination = destination;
            this.name = name;
        this.phone = phone;
        this.role="passenger";
    }


    public String getInception() {
        return inception;
    }

    public String getDestination() {
        return destination;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }
}
