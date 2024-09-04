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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.certificate.GetIssuingUnitIdFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.certificate.GetIssuingUnitIdFromWebcert;

@Service
@RequiredArgsConstructor
public class GetIssuingUnitIdAggregator {

    private final CertificateServiceProfile certificateServiceProfile;
    private final GetIssuingUnitIdFromWebcert getUnitIdFromWC;
    private final GetIssuingUnitIdFromCertificateService getUnitIdFromCS;

    public String get(String certificateId) {
        if (!certificateServiceProfile.active()) {
            return getUnitIdFromWC.get(certificateId);
        }

        final var unitIdFromCS = getUnitIdFromCS.get(certificateId);

        return unitIdFromCS != null ? unitIdFromCS : getUnitIdFromWC.get(certificateId);
    }
}
