package se.inera.webcert.web.controller.api.dto;

import se.inera.webcert.service.draft.dto.SigneringsBiljett;

public class BiljettResponse {
    private final String id;
    private final String status;
    private final String intygsId;

    public BiljettResponse(SigneringsBiljett biljett) {
        id = biljett.getId();
        status = biljett.getStatus();
        intygsId = biljett.getIntygsId();
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getIntygsId() {
        return intygsId;
    }
}
