package com.greenhouse.client.dto;

import java.util.List;

public class DriversResponse {

    private List<EspDriver> drivers;

    public List<EspDriver> getDrivers() {
        return drivers;
    }

    public void setDrivers(List<EspDriver> drivers) {
        this.drivers = drivers;
    }
}
