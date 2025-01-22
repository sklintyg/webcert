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

package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@RequiredArgsConstructor
public class CertificateDetailsUpdateService {

    private final AlternateSsnEvaluator alternateSsnEvaluator;
    private final MonitoringLogService monitoringLogService;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    public void update(Certificate certificate, WebCertUser user, Personnummer beforeAlternateSsn) {
        final var shouldUpdatePatientDetails = alternateSsnEvaluator.shouldUpdate(certificate, user);
        final var shouldSetExternalReference = shouldSetExternalReference(user, certificate);

        if (!shouldSetExternalReference && !shouldUpdatePatientDetails) {
            return;
        }

        final var savedCertificate = csIntegrationService.saveCertificate(
            csIntegrationRequestFactory.saveRequest(
                certificate,
                shouldUpdatePatientDetails
                    ? user.getParameters().getAlternateSsn()
                    : certificate.getMetadata().getPatient().getActualPersonId().getId(),
                shouldSetExternalReference
                    ? user.getParameters().getReference()
                    : null
            )
        );

        if (shouldUpdatePatientDetails) {
            monitoringLogService.logUtkastPatientDetailsUpdated(
                savedCertificate.getMetadata().getId(),
                savedCertificate.getMetadata().getType()
            );
            publishCertificateStatusUpdateService.publish(savedCertificate, HandelsekodEnum.ANDRAT);
            user.getParameters().setBeforeAlternateSsn(beforeAlternateSsn != null ? beforeAlternateSsn.getOriginalPnr()
                : certificate.getMetadata().getPatient().getPersonId().getId());
        }
    }

    private static boolean shouldSetExternalReference(WebCertUser user, Certificate certificate) {
        return certificate.getMetadata().getExternalReference() == null && user.getParameters() != null && (
            user.getParameters().getReference() != null && !user.getParameters().getReference().isBlank());
    }
}
