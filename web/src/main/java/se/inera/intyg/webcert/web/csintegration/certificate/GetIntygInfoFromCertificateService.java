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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.intyginfo.dto.WcIntygInfo;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.intyginfo.IntygInfoServiceInterface;

@Slf4j
@Service("getIntygInfoFromCS")
@RequiredArgsConstructor
public class GetIntygInfoFromCertificateService implements IntygInfoServiceInterface {

    private final CSIntegrationService csIntegrationService;
    private final CertificateToIntygInfoConverter certificateToIntygInfoConverter;

    @Override
    public Optional<WcIntygInfo> getIntygInfo(String certificateId) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
            return Optional.empty();
        }

        log.debug("Getting certificate from certificate service with id '{}'", certificateId);
        final var certificate = csIntegrationService.getInternalCertificate(
            certificateId
        );
        log.debug("Certificate with id '{}' was retrieved from certificate service", certificateId);

        final var questions = csIntegrationService.getQuestions(
            certificateId
        );

        return Optional.of(certificateToIntygInfoConverter.convert(certificate, questions));
    }
}
