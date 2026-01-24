package com.greenhouse.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LevelRequest {

    @JsonProperty("level")
    private Integer level;

    public LevelRequest() {
    }

    @JsonCreator
    public LevelRequest(@JsonProperty("level") Integer level) {
        this.level = level;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
