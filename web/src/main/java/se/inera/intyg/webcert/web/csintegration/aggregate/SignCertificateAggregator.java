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
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.SignCertificateFacadeService;

@Service("signCertificateAggregator")
public class SignCertificateAggregator implements SignCertificateFacadeService {

    private final CertificateServiceProfile certificateServiceProfile;
    private final SignCertificateFacadeService signCertificateFacadeServiceWC;
    private final SignCertificateFacadeService signCertificateFacadeServiceCS;

    public SignCertificateAggregator(CertificateServiceProfile certificateServiceProfile,
        @Qualifier("signCertificateFromWc") SignCertificateFacadeService signCertificateFacadeServiceWC,
        @Qualifier("signCertificateFromCS") SignCertificateFacadeService signCertificateFacadeServiceCS) {
        this.certificateServiceProfile = certificateServiceProfile;
        this.signCertificateFacadeServiceWC = signCertificateFacadeServiceWC;
        this.signCertificateFacadeServiceCS = signCertificateFacadeServiceCS;
    }

    @Override
    public Certificate signCertificate(Certificate certificate) {
        if (!certificateServiceProfile.active()) {
            return signCertificateFacadeServiceWC.signCertificate(certificate);
        }

        final var signedCertificate = signCertificateFacadeServiceCS.signCertificate(certificate);
        return signedCertificate != null ? signedCertificate : signCertificateFacadeServiceWC.signCertificate(certificate);
    }
}
