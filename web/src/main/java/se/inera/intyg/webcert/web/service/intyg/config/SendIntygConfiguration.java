package se.inera.intyg.webcert.web.service.intyg.config;

import java.text.MessageFormat;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SendIntygConfiguration {

    private static final String PATIENT_CONSENT_PATTERN = "Intyget skickat till mottagare {0} {1,choice,0#utan|1#med} patientens medgivande";

    private String recipient;

    private boolean patientConsent;

    private WebCertUser webCertUser;

    public SendIntygConfiguration() {

    }

    public SendIntygConfiguration(String recipient, boolean patientConsent, WebCertUser webCertUser) {
        super();
        this.recipient = recipient;
        this.patientConsent = patientConsent;
        this.webCertUser = webCertUser;
    }

    @JsonIgnore
    public String getPatientConsentMessage() {
        int hasConsent = (isPatientConsent()) ? 1 : 0;
        return MessageFormat.format(PATIENT_CONSENT_PATTERN, recipient, hasConsent);
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

    public WebCertUser getWebCertUser() {
        return webCertUser;
    }

    public void setWebCertUser(WebCertUser webCertUser) {
        this.webCertUser = webCertUser;
    }

}
