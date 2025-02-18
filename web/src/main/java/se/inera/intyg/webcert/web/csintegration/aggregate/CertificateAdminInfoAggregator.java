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

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.intyginfo.dto.WcIntygInfo;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.intyginfo.IntygInfoServiceInterface;

@Service("certificateAdminInfoAggregator")
public class CertificateAdminInfoAggregator implements IntygInfoServiceInterface {

    private final IntygInfoServiceInterface getCertificateAdminInfoFromWebcert;
    private final IntygInfoServiceInterface getCertificateAdminInfoFromCertificateService;
    private final CertificateServiceProfile certificateServiceProfile;

    public CertificateAdminInfoAggregator(
        @Qualifier("getCertificateAdminInfoFromWC")
        IntygInfoServiceInterface getCertificateInfoFromWebcert,
        @Qualifier("getCertificateAdminInfoFromCS")
        IntygInfoServiceInterface getCertificateInfoFromCertificateService,
        CertificateServiceProfile certificateServiceProfile) {
        this.getCertificateAdminInfoFromWebcert = getCertificateInfoFromWebcert;
        this.getCertificateAdminInfoFromCertificateService = getCertificateInfoFromCertificateService;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public Optional<WcIntygInfo> getIntygInfo(String intygId) {
        final var certificateFromWebcert = getCertificateAdminInfoFromWebcert.getIntygInfo(intygId);
        if (!certificateServiceProfile.active()) {
            return certificateFromWebcert;
        }

        final var certificateFromCertificateService = getCertificateAdminInfoFromCertificateService.getIntygInfo(intygId);

        return certificateFromCertificateService.isEmpty() ? certificateFromWebcert : certificateFromCertificateService;
    }
}
