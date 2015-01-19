package se.inera.webcert.web.controller.moduleapi.dto;

import se.inera.webcert.service.signatur.dto.SignaturTicket;

public class SignaturTicketResponse {
    private final String id;
    private final String status;
    private final String intygsId;
    private final String hash;

    public SignaturTicketResponse(SignaturTicket ticket) {
        id = ticket.getId();
        status = ticket.getStatus().name();
        intygsId = ticket.getIntygsId();
        hash = ticket.getHash();
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
