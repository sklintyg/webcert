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

package se.inera.intyg.webcert.web.csintegration.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
@RequiredArgsConstructor
public class PDLLogService {

    private final LogRequestFactory logRequestFactory;
    private final LogService logService;
    private final WebCertUserService webCertUserService;
    private static final String SJF_LOG_POST = "Läsning i enlighet med sammanhållen journalföring";

    public void logCreated(Certificate certificate) {
        logService.logCreateIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
        );
    }

    public void logCreatedWithIntygUser(Certificate certificate, IntygUser user) {
        logService.logCreateIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo()),
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
            logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
        );
    }

    public void logSaved(Certificate certificate) {
        logService.logUpdateIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
        );
    }

    public void logDeleted(Certificate certificate) {
        logService.logDeleteIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
        );
    }

    public void logSign(Certificate certificate) {
        logService.logSignIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
        );
    }

    public void logPrinted(Certificate certificate) {
        final var status = certificate.getMetadata().getStatus();
        if (status == CertificateStatus.UNSIGNED || status == CertificateStatus.LOCKED) {
            logService.logPrintIntygAsDraft(
                logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
            );
        }

        if (status == CertificateStatus.REVOKED) {
            logService.logPrintRevokedIntygAsPDF(
                logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
            );
        }

        if (status == CertificateStatus.SIGNED) {
            logService.logPrintIntygAsPDF(
                logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
            );
        }
    }

    public void logSent(Certificate certificate) {
        logService.logSendIntygToRecipient(
            logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfoWithRecipient(certificate, additionalInfo()))
        );
    }

    public void logRevoke(Certificate certificate) {
        logService.logRevokeIntyg(
            logRequestFactory.createLogRequestFromCertificate(certificate, additionalInfo())
        );
    }

    public void logCreateMessage(String personId, String certificateId) {
        logService.logCreateMessage(
            webCertUserService.getUser(),
            personId,
            certificateId
        );
    }

    private String additionalInfo() {
        final var user = webCertUserService.hasAuthenticationContext() ? webCertUserService.getUser() : null;
        if (user == null) {
            return null;
        }
        return user.getParameters() != null && user.getParameters().isSjf() ? SJF_LOG_POST : null;
    }

    private String additionalInfoWithRecipient(Certificate certificate, String additionalInfo) {
        final var recipientMessage = String.format("Intyg skickat till mottagare %s", certificate.getMetadata().getRecipient().getName());
        if (additionalInfo == null) {
            return recipientMessage;
        }
        return additionalInfo.concat(". ").concat(recipientMessage);
    }

    public void logReadLevelTwo(Certificate response) {
        logService.logReadLevelTwo(webCertUserService.getUser(), response.getMetadata().getPatient().getPersonId().getId());
    }
}
