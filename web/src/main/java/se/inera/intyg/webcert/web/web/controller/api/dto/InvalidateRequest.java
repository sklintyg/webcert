package se.inera.intyg.webcert.web.web.controller.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class InvalidateRequest {
    private String userHsaId;
    private String launchId;

    public String getUserHsaId() {
        return userHsaId;
    }

    public String getLaunchId() {
        return launchId;
    }

    public void setUserHsaId(String userHsaId) {
        this.userHsaId = userHsaId;
    }

    public void setLaunchId(String launchId) {
        this.launchId = launchId;
    }
    @JsonIgnore
    public boolean validateRequest() {
        return this.launchId != null && this.userHsaId != null;
    }

    @Override
    public String toString() {
        return "InvalidateRequest{" +
                "userHsaId='" + userHsaId + '\'' +
                ", launchId='" + launchId + '\'' +
                '}';
    }
}
