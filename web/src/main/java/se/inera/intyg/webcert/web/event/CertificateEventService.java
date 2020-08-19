/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

public interface CertificateEventService {

    void createCertificateEvent(String certificateId, String user, EventCode eventCode);

    void createCertificateEvent(String certificateId, String user, EventCode eventCode, String message);

    void createCertificateEventFromCopyUtkast(Utkast certificate, String user, EventCode eventCode, String originalCertificateId);

    List<CertificateEvent> getCertificateEvents(String certificateId);

}
