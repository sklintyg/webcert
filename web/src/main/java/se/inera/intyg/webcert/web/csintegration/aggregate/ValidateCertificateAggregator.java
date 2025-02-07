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
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;

@Service("validateCertificateAggregator")
public class ValidateCertificateAggregator implements ValidateCertificateFacadeService {

    private final ValidateCertificateFacadeService validateCertificateFromWC;
    private final ValidateCertificateFacadeService validateCertificateFromCS;
    private final CertificateServiceProfile certificateServiceProfile;

    public ValidateCertificateAggregator(
        @Qualifier("validateCertificateFromWC") ValidateCertificateFacadeService validateCertificateFromWC,
        @Qualifier("validateCertificateFromCS") ValidateCertificateFacadeService validateCertificateFromCS,
        CertificateServiceProfile certificateServiceProfile) {
        this.validateCertificateFromWC = validateCertificateFromWC;
        this.validateCertificateFromCS = validateCertificateFromCS;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public ValidationErrorDTO[] validate(Certificate certificate) {
        if (!certificateServiceProfile.active()) {
            return validateCertificateFromWC.validate(certificate);
        }

        final var responseFromCS = validateCertificateFromCS.validate(certificate);

        return responseFromCS != null ? responseFromCS : validateCertificateFromWC.validate(certificate);
    }
}
