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
import se.inera.intyg.webcert.web.service.facade.ForwardCertificateFacadeService;

@Service("forwardCertificateAggregator")
public class ForwardCertificateAggregator implements ForwardCertificateFacadeService {

    private final ForwardCertificateFacadeService forwardCertificateFromWC;
    private final ForwardCertificateFacadeService forwardCertificateFromCS;
    private final CertificateServiceProfile certificateServiceProfile;

    public ForwardCertificateAggregator(
        @Qualifier("forwardCertificateFromWC")
        ForwardCertificateFacadeService forwardCertificateFromWC,
        @Qualifier("forwardCertificateFromCS")
        ForwardCertificateFacadeService forwardCertificateFromCS,
        CertificateServiceProfile certificateServiceProfile) {
        this.forwardCertificateFromWC = forwardCertificateFromWC;
        this.forwardCertificateFromCS = forwardCertificateFromCS;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public Certificate forwardCertificate(String certificateId, boolean forwarded) {
        if (!certificateServiceProfile.active()) {
            return forwardCertificateFromWC.forwardCertificate(certificateId, forwarded);
        }

        final var responseFromCS = forwardCertificateFromCS.forwardCertificate(certificateId, forwarded);

        return responseFromCS != null ? responseFromCS : forwardCertificateFromWC.forwardCertificate(certificateId, forwarded);
    }
}
