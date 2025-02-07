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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.IntegrationService;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationServiceForCS implements IntegrationService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final CertificateDetailsUpdateService certificateDetailsUpdateService;
    private final LogSjfService logSjfService;

    @Override
    public PrepareRedirectToIntyg prepareRedirectToIntyg(String certificateId, WebCertUser user) {
        return prepareRedirectToIntyg(certificateId, user, null);
    }

    @Override
    public PrepareRedirectToIntyg prepareRedirectToIntyg(String certificateId, WebCertUser user,
        Personnummer prepareBeforeAlternateSsn) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
            return null;
        }

        try {
            final var certificate = csIntegrationService.getCertificate(
                certificateId,
                csIntegrationRequestFactory.getCertificateRequest()
            );

            if (user.isSjfActive()) {
                logSjfService.log(certificate, user);
            }

            certificateDetailsUpdateService.update(certificate, user, prepareBeforeAlternateSsn);

            return createPrepareRedirectToIntyg(certificate);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, String.format("User '%s' was denied"
                    + "access to certificate '%s' from certificate-service.", user.getHsaId(), certificateId), e);
            }
            throw e;
        }
    }

    private static PrepareRedirectToIntyg createPrepareRedirectToIntyg(Certificate certificate) {
        final var redirectToIntyg = new PrepareRedirectToIntyg();
        redirectToIntyg.setIntygId(certificate.getMetadata().getId());
        redirectToIntyg.setIntygTyp(certificate.getMetadata().getType());
        redirectToIntyg.setIntygTypeVersion(certificate.getMetadata().getTypeVersion());
        return redirectToIntyg;
    }
}
