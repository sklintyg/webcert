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

package se.inera.intyg.webcert.web.csintegration.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;

@Service
@RequiredArgsConstructor
public class PDLLogService {

    private final LogRequestFactory logRequestFactory;
    private final LogService logService;

    public void logCreated(Certificate certificate) {
        logService.logCreateIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate)
        );
    }

    public void logCreatedWithIntygUser(Certificate certificate, IntygUser user) {
        logService.logCreateIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate),
            new LogUser.Builder(user.getHsaId(), user.getValdVardenhet().getId(), user.getValdVardgivare().getId())
                .userName(user.getNamn())
                .userAssignment(user.getSelectedMedarbetarUppdragNamn())
                .userTitle(user.getTitel())
                .enhetsNamn(user.getValdVardenhet().getNamn())
                .vardgivareNamn(user.getValdVardgivare().getNamn())
                .build()
        );
    }

    public void logRead(Certificate certificate) {
        logService.logReadIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate)
        );
    }

    public void logSaved(Certificate certificate) {
        logService.logUpdateIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate)
        );
    }

    public void logDeleted(Certificate certificate) {
        logService.logDeleteIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate)
        );
    }

    public void logSign(Certificate certificate) {
        logService.logSignIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate)
        );
    }

    public void logPrinted(Certificate certificate) {
        final var status = certificate.getMetadata().getStatus();
        if (status == CertificateStatus.UNSIGNED || status == CertificateStatus.LOCKED) {
            logService.logPrintIntygAsDraft(
                logRequestFactory.createLogRequestFromCertificate(certificate)
            );
        }

        if (status == CertificateStatus.REVOKED) {
            logService.logPrintRevokedIntygAsPDF(
                logRequestFactory.createLogRequestFromCertificate(certificate)
            );
        }

        if (status == CertificateStatus.SIGNED) {
            logService.logPrintIntygAsPDF(
                logRequestFactory.createLogRequestFromCertificate(certificate)
            );
        }
    }

    public void logSent(Certificate certificate) {
        logService.logSendIntygToRecipient(
            logRequestFactory.createLogRequestFromCertificate(certificate)
        );
    }

    public void logRevoke(Certificate certificate) {
        logService.logRevokeIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate)
        );
    }
}
