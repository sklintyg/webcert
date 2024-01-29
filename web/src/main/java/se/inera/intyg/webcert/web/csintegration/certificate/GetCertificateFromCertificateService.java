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

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;

@Service
public class GetCertificateFromCertificateService implements GetCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CertificateServiceUserHelper certificateServiceUserHelper;
    private final CertificateServiceUnitHelper certificateServiceUnitHelper;

    public GetCertificateFromCertificateService(CSIntegrationService csIntegrationService,
        CertificateServiceUserHelper certificateServiceUserHelper, CertificateServiceUnitHelper certificateServiceUnitHelper) {
        this.csIntegrationService = csIntegrationService;
        this.certificateServiceUserHelper = certificateServiceUserHelper;
        this.certificateServiceUnitHelper = certificateServiceUnitHelper;
    }

    @Override
    public Certificate getCertificate(String certificateId, boolean pdlLog, boolean validateAccess) {
        return csIntegrationService.getCertificate(certificateId, createRequest());
    }

    private GetCertificateRequestDTO createRequest() {
        final var request = new GetCertificateRequestDTO();

        request.setUnit(certificateServiceUnitHelper.getUnit());
        request.setCareUnit(certificateServiceUnitHelper.getCareUnit());
        request.setCareProvider(certificateServiceUnitHelper.getCareProvider());
        request.setUser(certificateServiceUserHelper.get());

        return request;
    }
}
