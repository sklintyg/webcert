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
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.web.controller.internalapi.CertificateInteralApi;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@Service("certificateInternalAggregator")
public class CertificateInternalAggregator implements CertificateInteralApi {

    private final CertificateServiceProfile certificateServiceProfile;
    private final CertificateInteralApi certificateInternalServiceFromWC;
    private final CertificateInteralApi certificateInternalServiceFromCS;

    public CertificateInternalAggregator(CertificateServiceProfile certificateServiceProfile,
        @Qualifier("certificateInternalServiceFromWC") CertificateInteralApi certificateInteralServiceFromWC,
        @Qualifier("certificateInternalServiceFromCS") CertificateInteralApi certificateInternalServiceFromCS) {
        this.certificateServiceProfile = certificateServiceProfile;
        this.certificateInternalServiceFromWC = certificateInteralServiceFromWC;
        this.certificateInternalServiceFromCS = certificateInternalServiceFromCS;
    }

    @Override
    public GetCertificateResponse get(String certificateId, String personId) {
        if (!certificateServiceProfile.active()) {
            return certificateInternalServiceFromWC.get(certificateId, personId);
        }

        final var responseFromCS = certificateInternalServiceFromCS.get(certificateId, personId);

        return responseFromCS != null ? responseFromCS : certificateInternalServiceFromWC.get(certificateId, personId);
    }
}
