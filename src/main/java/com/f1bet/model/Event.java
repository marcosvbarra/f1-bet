package com.f1bet.model;

import java.util.List;

public class Event {

    private Integer sessionKey;
    private String sessionName;
    private String sessionType;
    private Integer year;
    private String country;
    private List<Driver> driverMarket;

    public Integer getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(Integer sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<Driver> getDriverMarket() {
        return driverMarket;
    }

    public void setDriverMarket(List<Driver> driverMarket) {
        this.driverMarket = driverMarket;
    }
}