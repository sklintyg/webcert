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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.certificate.PrintCertificateFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;

@Service
@RequiredArgsConstructor
public class PrintCertificateAggregator {

    private final CertificateServiceProfile certificateServiceProfile;
    private final IntygService intygService;
    private final PrintCertificateFromCertificateService printCertificateFromCertificateService;

    public IntygPdf get(String certificateId, String certificateType, boolean isEmployerCopy) {
        if (!certificateServiceProfile.active()) {
            return intygService.fetchIntygAsPdf(certificateId, certificateType, isEmployerCopy);
        }

        final var response = printCertificateFromCertificateService.print(certificateId, certificateType, isEmployerCopy);

        return response != null
            ? response
            : intygService.fetchIntygAsPdf(certificateId, certificateType, isEmployerCopy);
    }
}
