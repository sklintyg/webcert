package se.inera.intyg.webcert.web.web.controller.facade.dto;

public class RevokeCertificateDTO {

    private String message;
    private String reason;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
