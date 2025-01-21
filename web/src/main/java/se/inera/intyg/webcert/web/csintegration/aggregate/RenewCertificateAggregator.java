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
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;

@Service("renewCertificateAggregator")
public class RenewCertificateAggregator implements RenewCertificateFacadeService {

    private final RenewCertificateFacadeService renewCertificateFromWebcert;
    private final RenewCertificateFacadeService renewCertificateFromCertificateService;
    private final CertificateServiceProfile certificateServiceProfile;

    public RenewCertificateAggregator(
        @Qualifier("renewCertificateFromWebcert")
        RenewCertificateFacadeService renewCertificateFromWebcert,
        @Qualifier("renewCertificateFromCertificateService")
        RenewCertificateFacadeService renewCertificateFromCertificateService,
        CertificateServiceProfile certificateServiceProfile) {
        this.renewCertificateFromWebcert = renewCertificateFromWebcert;
        this.renewCertificateFromCertificateService = renewCertificateFromCertificateService;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public String renewCertificate(String certificateId) {
        if (!certificateServiceProfile.active()) {
            return renewCertificateFromWebcert.renewCertificate(certificateId);
        }

        final var responseFromCS = renewCertificateFromCertificateService.renewCertificate(certificateId);

        return responseFromCS != null ? responseFromCS : renewCertificateFromWebcert.renewCertificate(certificateId);
    }
}
