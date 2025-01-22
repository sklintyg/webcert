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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.GetCertificateEventsFacadeService;

@Service("getCertificateEventsAggregator")
public class GetCertificateEventsAggregator implements GetCertificateEventsFacadeService {

    private final GetCertificateEventsFacadeService getCertificateEventsFromWC;
    private final GetCertificateEventsFacadeService getCertificateEventsFromCS;
    private final CertificateServiceProfile certificateServiceProfile;

    public GetCertificateEventsAggregator(
        @Qualifier("getCertificateEventsFromWebcert")
        GetCertificateEventsFacadeService getCertificateEventsFromWC,
        @Qualifier("getCertificateEventsFromCertificateService")
        GetCertificateEventsFacadeService replaceCertificateFromCertificateService,
        CertificateServiceProfile certificateServiceProfile) {
        this.getCertificateEventsFromWC = getCertificateEventsFromWC;
        this.getCertificateEventsFromCS = replaceCertificateFromCertificateService;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public CertificateEventDTO[] getCertificateEvents(String certificateId) {
        if (!certificateServiceProfile.active()) {
            return getCertificateEventsFromWC.getCertificateEvents(certificateId);
        }

        final var responseFromCS = getCertificateEventsFromCS.getCertificateEvents(certificateId);

        return responseFromCS != null ? responseFromCS : getCertificateEventsFromWC.getCertificateEvents(certificateId);
    }
}
