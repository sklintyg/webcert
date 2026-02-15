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
package se.inera.intyg.webcert.persistence.event.repository;

import java.time.LocalDateTime;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;

public class CertificateEventTestUtil {

    private CertificateEventTestUtil() {
    }

    public static final String CERTIFICATE_ID_1 = "id1";
    public static final String CERTIFICATE_ID_2 = "id2";

    public static final String USER = "SE123344332";

    public static final EventCode EVENT_CODE_SKAPAT = EventCode.SKAPAT;
    public static final EventCode EVENT_CODE_SIGNAT = EventCode.SIGNAT;

    public static final String MESSAGE = "Really important event.";

    public static CertificateEvent buildCertificateEvent(String certificateId) {
        return buildCertificateEvent(certificateId, USER, EVENT_CODE_SKAPAT, MESSAGE);
    }

    public static CertificateEvent buildCertificateEvent(String certificateId, String user) {
        return buildCertificateEvent(certificateId, user, EVENT_CODE_SKAPAT, MESSAGE);
    }

    public static CertificateEvent buildCertificateEvent(String certificateId, EventCode eventCode) {
        return buildCertificateEvent(certificateId, USER, eventCode, MESSAGE);
    }

    public static CertificateEvent buildCertificateEvent(String certificateId, String user, EventCode eventCode) {
        return buildCertificateEvent(certificateId, user, eventCode, MESSAGE);
    }

    public static CertificateEvent buildCertificateEvent(String certificateId, String user, EventCode eventCode, String message) {
        CertificateEvent certificateEvent = new CertificateEvent();
        certificateEvent.setCertificateId(certificateId);
        certificateEvent.setUser(user);
        certificateEvent.setEventCode(eventCode);
        certificateEvent.setTimestamp(LocalDateTime.now());
        certificateEvent.setMessage(message);

        return certificateEvent;
    }
}
