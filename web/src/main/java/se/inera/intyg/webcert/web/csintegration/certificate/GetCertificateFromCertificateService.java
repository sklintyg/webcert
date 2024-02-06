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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;

@Service("GetCertificateFromCS")
public class GetCertificateFromCertificateService implements GetCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateFromCertificateService.class);

    private final CSIntegrationService csIntegrationService;
    private final CertificateServiceUserHelper certificateServiceUserHelper;
    private final CertificateServiceUnitHelper certificateServiceUnitHelper;
    private final PDLLogService pdlLogService;

    public GetCertificateFromCertificateService(CSIntegrationService csIntegrationService,
        CertificateServiceUserHelper certificateServiceUserHelper, CertificateServiceUnitHelper certificateServiceUnitHelper,
        PDLLogService pdlLogService) {
        this.csIntegrationService = csIntegrationService;
        this.certificateServiceUserHelper = certificateServiceUserHelper;
        this.certificateServiceUnitHelper = certificateServiceUnitHelper;
        this.pdlLogService = pdlLogService;
    }

    @Override
    public Certificate getCertificate(String certificateId, boolean pdlLog, boolean validateAccess) {
        if (Boolean.FALSE.equals(csIntegrationService.certificateExists(certificateId))) {
            LOG.info("Certificate with id '{}' does not exist in certificate service", certificateId);
            return null;
        }

        LOG.info("Getting certificate from certificate service with id '{}'", certificateId);
        final var response = csIntegrationService.getCertificate(certificateId, createRequest());
        LOG.info("Certificate with id '{}' was retrieved from certificate service", certificateId);

        if (pdlLog) {
            pdlLogService.logRead(
                response.getMetadata().getPatient().getPersonId().getId(),
                response.getMetadata().getId()
            );
        }

        return response;
    }

    private GetCertificateRequestDTO createRequest() {
        return GetCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }
}
