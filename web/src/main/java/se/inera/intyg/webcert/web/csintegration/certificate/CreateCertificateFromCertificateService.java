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
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;

@Service("CreateCertificateFromCS")
public class CreateCertificateFromCertificateService implements CreateCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateCertificateFromCertificateService.class);

    private final CSIntegrationService csIntegrationService;
    private final CertificateServiceUserHelper certificateServiceUserHelper;
    private final CertificateServiceUnitHelper certificateServiceUnitHelper;
    private final CertificateServicePatientHelper certificateServicePatientHelper;
    private final IntygTextsService intygTextsService;

    public CreateCertificateFromCertificateService(CSIntegrationService csIntegrationService,
        CertificateServiceUserHelper certificateServiceUserHelper, CertificateServiceUnitHelper certificateServiceUnitHelper,
        CertificateServicePatientHelper certificateServicePatientHelper, IntygTextsService intygTextsService) {
        this.csIntegrationService = csIntegrationService;
        this.certificateServiceUserHelper = certificateServiceUserHelper;
        this.certificateServiceUnitHelper = certificateServiceUnitHelper;
        this.certificateServicePatientHelper = certificateServicePatientHelper;
        this.intygTextsService = intygTextsService;
    }

    @Override
    public String create(String certificateType, String patientId) throws CreateCertificateException {
        final var request = createRequest(certificateType, patientId);

        LOG.debug("Attempting to create certificate of type '{}'", certificateType);

        final var response = csIntegrationService.createCertificate(request);

        if (response == null) {
            throw new CreateCertificateException("Could not create certificate, received null");
        }

        return response.getMetadata().getId();
    }

    private CreateCertificateRequestDTO createRequest(String certificateType, String patientId) throws CreateCertificateException {
        final var request = new CreateCertificateRequestDTO();
        final var certificateModelId = new CertificateModelIdDTO(certificateType, intygTextsService.getLatestVersion(certificateType));

        request.setUnit(certificateServiceUnitHelper.getUnit());
        request.setCareUnit(certificateServiceUnitHelper.getCareUnit());
        request.setCareProvider(certificateServiceUnitHelper.getCareProvider());
        request.setPatient(certificateServicePatientHelper.get(createPatientId(patientId)));
        request.setUser(certificateServiceUserHelper.get());
        request.setCertificateModelIdDTO(certificateModelId);

        return request;
    }

    private Personnummer createPatientId(String patientId) throws CreateCertificateException {
        final var convertedPatientId = Personnummer.createPersonnummer(patientId);

        if (convertedPatientId.isEmpty()) {
            throw new CreateCertificateException("PatientId has wrong format");
        }

        return convertedPatientId.get();
    }
}
