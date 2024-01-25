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

package se.inera.intyg.webcert.web.csintegration.typeinfo;

import java.util.List;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@Service("getCertificateTypeInfoFromCertificateService")
public class GetCertificateTypeInfoFromCertificateService implements GetCertificateTypesFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CertificateServiceUserHelper certificateServiceUserHelper;
    private final CertificateServicePatientHelper certificateServicePatientHelper;
    private final CertificateServiceUnitHelper certificateServiceUnitHelper;

    public GetCertificateTypeInfoFromCertificateService(CSIntegrationService csIntegrationService,
        CertificateServiceUserHelper certificateServiceUserHelper, CertificateServicePatientHelper certificateServicePatientHelper,
        CertificateServiceUnitHelper certificateServiceUnitHelper) {
        this.csIntegrationService = csIntegrationService;
        this.certificateServiceUserHelper = certificateServiceUserHelper;
        this.certificateServicePatientHelper = certificateServicePatientHelper;
        this.certificateServiceUnitHelper = certificateServiceUnitHelper;
    }

    public List<CertificateTypeInfoDTO> get(Personnummer patientId) {
        final var request = createRequest(patientId);
        return csIntegrationService.getTypeInfo(request);
    }

    private CertificateServiceTypeInfoRequestDTO createRequest(Personnummer patientId) {
        final var request = new CertificateServiceTypeInfoRequestDTO();

        request.setUser(certificateServiceUserHelper.get());
        request.setPatient(certificateServicePatientHelper.get(patientId));
        request.setUnit(certificateServiceUnitHelper.getUnit());
        request.setCareUnit(certificateServiceUnitHelper.getCareUnit());
        request.setCareProvider(certificateServiceUnitHelper.getCareProvider());
        return request;
    }
}
