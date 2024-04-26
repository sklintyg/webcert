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

package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
@RequiredArgsConstructor
public class FinalizeCertificateSignService {

    private final PDLLogService pdlLogService;
    private final WebCertUserService webCertUserService;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    public void finalizeSign(Certificate certificate) {
        final var user = webCertUserService.getUser();

        monitoringLogService.logIntygSigned(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            user.getHsaId(),
            user.getAuthenticationScheme(),
            null
        );

        pdlLogService.logSign(
            certificate
        );

        publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SIGNAT);
    }
}
