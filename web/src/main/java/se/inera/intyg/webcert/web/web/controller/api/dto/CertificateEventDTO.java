/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.time.LocalDateTime;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;

public class CertificateEventDTO {

    private String certificateId;
    private String user;
    private EventCode eventCode;
    private LocalDateTime timestamp;
    private String message;
    private ExtendedEventMessage extendedMessage;

    public CertificateEventDTO(CertificateEvent event) {
        this.certificateId = event.getCertificateId();
        this.user = event.getUser();
        this.eventCode = event.getEventCode();
        this.timestamp = event.getTimestamp();
        this.message = event.getMessage();
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public EventCode getEventCode() {
        return eventCode;
    }

    public void setEventCode(EventCode eventCode) {
        this.eventCode = eventCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExtendedEventMessage getExtendedMessage() {
        return extendedMessage;
    }

    public void setExtendedMessage(ExtendedEventMessage extendedMessage) {
        this.extendedMessage = extendedMessage;
    }

    public static class ExtendedEventMessage {

        private String originalCertificateId;
        private String originalCertificateType;
        private String originalCertificateTypeVersion;

        public String getOriginalCertificateId() {
            return originalCertificateId;
        }

        public void setOriginalCertificateId(String originalCertificateId) {
            this.originalCertificateId = originalCertificateId;
        }

        public String getOriginalCertificateType() {
            return originalCertificateType;
        }

        public void setOriginalCertificateType(String originalCertificateType) {
            this.originalCertificateType = originalCertificateType;
        }

        public String getOriginalCertificateTypeVersion() {
            return originalCertificateTypeVersion;
        }

        public void setOriginalCertificateTypeVersion(String originalCertificateTypeVersion) {
            this.originalCertificateTypeVersion = originalCertificateTypeVersion;
        }
    }

}


