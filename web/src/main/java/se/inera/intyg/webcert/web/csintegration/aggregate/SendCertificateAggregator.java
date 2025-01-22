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
import se.inera.intyg.webcert.web.service.facade.SendCertificateFacadeService;

@Service("sendCertificateAggregator")
public class SendCertificateAggregator implements SendCertificateFacadeService {

    private final SendCertificateFacadeService sendCertificateFromWebcert;
    private final SendCertificateFacadeService sendCertificateFromCertificateService;
    private final CertificateServiceProfile certificateServiceProfile;

    public SendCertificateAggregator(
        @Qualifier("sendCertificateFromWebcert")
        SendCertificateFacadeService sendCertificateFromWebcert,
        @Qualifier("sendCertificateFromCertificateService")
        SendCertificateFacadeService sendCertificateFromCertificateService,
        CertificateServiceProfile certificateServiceProfile) {
        this.sendCertificateFromWebcert = sendCertificateFromWebcert;
        this.sendCertificateFromCertificateService = sendCertificateFromCertificateService;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public String sendCertificate(String certificateId) {
        if (!certificateServiceProfile.active()) {
            return sendCertificateFromWebcert.sendCertificate(certificateId);
        }

        final var responseFromCS = sendCertificateFromCertificateService.sendCertificate(certificateId);

        return responseFromCS != null ? responseFromCS : sendCertificateFromWebcert.sendCertificate(certificateId);
    }
}
