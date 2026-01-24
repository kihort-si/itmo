package com.greenhouse.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InfoResponse {

    @JsonProperty("module-id")
    private Integer moduleId;

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }
}
