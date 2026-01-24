package com.greenhouse.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BindResponse {

    @JsonProperty("driverId")
    private Integer driverId;

    public BindResponse() {
    }

    @JsonCreator
    public BindResponse(@JsonProperty("driverId") Integer driverId) {
        this.driverId = driverId;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }
}
