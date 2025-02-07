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
package se.inera.intyg.webcert.web.event;

import java.util.List;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

/**
 * Service to read and add events on certificates.
 */
public interface CertificateEventService {

    /**
     * Creates an event.
     *
     * @param certificateId Id of the certificate that an event is registered on
     * @param user User or system that is triggering the event
     * @param eventCode Type of event triggered
     */
    void createCertificateEvent(String certificateId, String user, EventCode eventCode);

    /**
     * Creates an event.
     *
     * @param certificateId Id of the certificate that an event is registered on
     * @param user User or system that is triggering the event
     * @param eventCode Type of event triggered
     * @param message Optional description
     */
    void createCertificateEvent(String certificateId, String user, EventCode eventCode, String message);

    /**
     * Creates an event.
     *
     * Use when generating an event from the creation of a certificate in a relation.
     *
     * @param certificate Certificate that the event will be registered on
     * @param user User or system that is triggering the event
     * @param eventCode Type of event triggered
     * @param originalCertificateId Id of the certificate that has a relation to the certificate event
     */
    void createCertificateEventFromCopyUtkast(Utkast certificate, String user, EventCode eventCode, String originalCertificateId);

    /**
     * Returns a list of the events for a certificate.
     *
     * @param certificateId Id of the certificate
     * @return List of events. Empty list if there where no events to find.
     */
    List<CertificateEvent> getCertificateEvents(String certificateId);
}
