package se.inera.intyg.webcert.web.web.controller.facade.dto;

public class ForwardCertificateRequestDTO {

    private boolean forward;

    public boolean isForwarded() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }
}
