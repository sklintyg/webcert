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
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.ComplementCertificateFacadeService;

@Service("complementCertificateAggregator")
public class ComplementCertificateAggregator implements ComplementCertificateFacadeService {

    private final ComplementCertificateFacadeService complementCertificateFromWebcert;
    private final ComplementCertificateFacadeService complementCertificateFromCertificateService;
    private final CertificateServiceProfile certificateServiceProfile;

    public ComplementCertificateAggregator(
        @Qualifier("complementCertificateFromWebcert")
        ComplementCertificateFacadeService replaceCertificateFromWebcert,
        @Qualifier("complementCertificateFromCertificateService")
        ComplementCertificateFacadeService replaceCertificateFromCertificateService,
        CertificateServiceProfile certificateServiceProfile) {
        this.complementCertificateFromWebcert = replaceCertificateFromWebcert;
        this.complementCertificateFromCertificateService = replaceCertificateFromCertificateService;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public Certificate complement(String certificateId, String message) {
        if (!certificateServiceProfile.active()) {
            return complementCertificateFromWebcert.complement(certificateId, message);
        }

        final var responseFromCS = complementCertificateFromCertificateService.complement(certificateId, message);

        return responseFromCS != null ? responseFromCS : complementCertificateFromWebcert.complement(certificateId, message);
    }

    @Override
    public Certificate answerComplement(String certificateId, String message) {
        if (!certificateServiceProfile.active()) {
            return complementCertificateFromWebcert.answerComplement(certificateId, message);
        }

        final var responseFromCS = complementCertificateFromCertificateService.answerComplement(certificateId, message);

        return responseFromCS != null ? responseFromCS : complementCertificateFromWebcert.answerComplement(certificateId, message);
    }
}
