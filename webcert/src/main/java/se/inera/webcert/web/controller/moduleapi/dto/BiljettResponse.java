package se.inera.webcert.web.controller.moduleapi.dto;

import se.inera.webcert.service.draft.dto.SignatureTicket;

public class BiljettResponse {
    private final String id;
    private final String status;
    private final String intygsId;
    private final String hash;

    public BiljettResponse(SignatureTicket biljett) {
        id = biljett.getId();
        status = biljett.getStatus().name();
        intygsId = biljett.getIntygsId();
        hash = biljett.getHash();
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

    public String getHash() {
        return hash;
    }
}
