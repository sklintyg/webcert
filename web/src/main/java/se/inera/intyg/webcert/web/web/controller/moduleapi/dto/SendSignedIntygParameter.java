package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

public class SendSignedIntygParameter {

    private String recipient;

    private boolean patientConsent;

    public SendSignedIntygParameter() {

    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isPatientConsent() {
        return patientConsent;
    }

    public void setPatientConsent(boolean patientConsent) {
        this.patientConsent = patientConsent;
    }
}
