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
import se.inera.intyg.webcert.web.web.controller.internalapi.GetCertificateInteralApi;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@Service("getCertificateInternalAggregator")
public class GetCertificateInternalAggregator implements GetCertificateInteralApi {

    private final GetCertificateInteralApi certificateInternalServiceFromWC;
    private final GetCertificateInteralApi certificateInternalServiceFromCS;

    public GetCertificateInternalAggregator(
        @Qualifier("getCertificateInternalServiceFromWC") GetCertificateInteralApi getCertificateInteralServiceFromWC,
        @Qualifier("getCertificateInternalServiceFromCS") GetCertificateInteralApi getCertificateInternalServiceFromCS) {
        this.certificateInternalServiceFromWC = getCertificateInteralServiceFromWC;
        this.certificateInternalServiceFromCS = getCertificateInternalServiceFromCS;
    }

    @Override
    public GetCertificateResponse get(String certificateId, String personId) {
        final var responseFromCS = certificateInternalServiceFromCS.get(certificateId, personId);

        return responseFromCS != null ? responseFromCS : certificateInternalServiceFromWC.get(certificateId, personId);
    }
}
