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
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetCertificatePdfService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;

@Service("getCertificateInternalPdfAggregator")
public class GetGetCertificateInternalPdfAggregator implements GetCertificatePdfService {

    private final CertificateServiceProfile certificateServiceProfile;
    private final GetCertificatePdfService getCertificateInternalPdfFromWC;
    private final GetCertificatePdfService getCertificateInternalPdfFromCS;

    public GetGetCertificateInternalPdfAggregator(CertificateServiceProfile certificateServiceProfile,
        @Qualifier("getCertificateInternalPdfFromWC") GetCertificatePdfService getCertificateInternalPdfFromWC,
        @Qualifier("getCertificateInternalPdfFromCS") GetCertificatePdfService getCertificateInternalPdfFromCS) {
        this.certificateServiceProfile = certificateServiceProfile;
        this.getCertificateInternalPdfFromWC = getCertificateInternalPdfFromWC;
        this.getCertificateInternalPdfFromCS = getCertificateInternalPdfFromCS;
    }


    @Override
    public CertificatePdfResponseDTO get(String customizationId, String certificateId, String personId) {
        if (!certificateServiceProfile.active()) {
            return getCertificateInternalPdfFromWC.get(customizationId, certificateId, personId);
        }

        final var responseFromCS = getCertificateInternalPdfFromCS.get(customizationId, certificateId, personId);

        return responseFromCS != null ? responseFromCS : getCertificateInternalPdfFromWC.get(customizationId, certificateId, personId);
    }
}
