/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public class SendIntygConfiguration {

    private static final String PATIENT_CONSENT_PATTERN = "Intyg skickat till mottagare {0}";

    private String recipient;

    private WebCertUser webCertUser;

    public SendIntygConfiguration() {
        // Needed for deserialization
    }

    public SendIntygConfiguration(String recipient, WebCertUser webCertUser) {
        super();
        this.recipient = recipient;
        this.webCertUser = webCertUser;
    }

    @JsonIgnore
    public String getPatientConsentMessage() {
        return MessageFormat.format(PATIENT_CONSENT_PATTERN, recipient);
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public WebCertUser getWebCertUser() {
        return webCertUser;
    }

    public void setWebCertUser(WebCertUser webCertUser) {
        this.webCertUser = webCertUser;
    }

}
