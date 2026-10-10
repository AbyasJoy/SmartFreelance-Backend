package com.smartfreelance.dto;

import jakarta.validation.constraints.NotNull;

public class UserStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;

    public UserStatusUpdateRequest() {
    }

    public UserStatusUpdateRequest(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
