/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;

@Service("GetCertificateAggregator")
public class GetCertificateAggregator implements GetCertificateFacadeService {

    private final GetCertificateFacadeService getCertificateFromWC;
    private final GetCertificateFacadeService getCertificateFromCS;
    private final Environment environment;

    public GetCertificateAggregator(
        @Qualifier("GetCertificateFromWC") GetCertificateFacadeService getCertificateFromWC,
        @Qualifier("GetCertificateFromCS") GetCertificateFacadeService getCertificateFromCS, Environment environment) {
        this.getCertificateFromWC = getCertificateFromWC;
        this.getCertificateFromCS = getCertificateFromCS;
        this.environment = environment;
    }

    @Override
    public Certificate getCertificate(String certificateId, boolean pdlLog, boolean validateAccess) {
        if (!environment.matchesProfiles("certificate-service-active")) {
            return getCertificateFromWC.getCertificate(certificateId, pdlLog, validateAccess);
        }

        final var responseFromCS = getCertificateFromCS.getCertificate(certificateId, pdlLog, validateAccess);

        return responseFromCS != null
            ? responseFromCS
            : getCertificateFromWC.getCertificate(certificateId, pdlLog, validateAccess);
    }
}
