/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
