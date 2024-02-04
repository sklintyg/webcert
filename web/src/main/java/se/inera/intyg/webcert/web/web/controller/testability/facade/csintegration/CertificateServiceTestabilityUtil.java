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

package se.inera.intyg.webcert.web.web.controller.testability.facade.csintegration;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.webcert.web.csintegration.certificate.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserRole;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;

@Component
public class CertificateServiceTestabilityUtil {

    private final CertificateServicePatientHelper certificateServicePatientHelper;
    private final CSTestabilityIntegrationService csTestabilityIntegrationService;

    public CertificateServiceTestabilityUtil(CertificateServicePatientHelper certificateServicePatientHelper,
        CSTestabilityIntegrationService csTestabilityIntegrationService) {
        this.certificateServicePatientHelper = certificateServicePatientHelper;
        this.csTestabilityIntegrationService = csTestabilityIntegrationService;
    }

    public String create(CreateNewDraftRequest createNewDraftRequest, CertificateModelIdDTO modelId) {
        final var certificateRequestDTO = createRequest(createNewDraftRequest, modelId);
        final var certificate = csTestabilityIntegrationService.createCertificate(certificateRequestDTO);
        return certificate.getMetadata().getId();
    }

    private CreateCertificateRequestDTO createRequest(CreateNewDraftRequest createNewDraftRequest,
        CertificateModelIdDTO modelId) {
        final var certificateRequestDTO = new CreateCertificateRequestDTO();
        certificateRequestDTO.setUser(buildUser(createNewDraftRequest));
        certificateRequestDTO.setPatient(
            certificateServicePatientHelper.get(createNewDraftRequest.getPatient().getPersonId())
        );
        certificateRequestDTO.setUnit(convertToUnit(createNewDraftRequest.getHosPerson().getVardenhet()));
        certificateRequestDTO.setCareUnit(convertToUnit(createNewDraftRequest.getHosPerson().getVardenhet()));
        certificateRequestDTO.setCareProvider(convertToCareProvider(createNewDraftRequest.getHosPerson().getVardenhet().getVardgivare()));
        certificateRequestDTO.setCertificateModelId(modelId);
        return certificateRequestDTO;
    }

    private static CertificateServiceUserDTO buildUser(CreateNewDraftRequest createNewDraftRequest) {
        return CertificateServiceUserDTO.create(createNewDraftRequest.getHosPerson().getPersonId(), CertificateServiceUserRole.DOCTOR,
            false);
    }

    private static CertificateServiceUnitDTO convertToCareProvider(Vardgivare vardgivare) {
        final var careProvider = new CertificateServiceUnitDTO();
        careProvider.setId(vardgivare.getVardgivarid());
        careProvider.setName(vardgivare.getVardgivarnamn());
        return careProvider;
    }

    private static CertificateServiceUnitDTO convertToUnit(Vardenhet vardenhet) {
        final var unit = new CertificateServiceUnitDTO();
        unit.setId(vardenhet.getEnhetsid());
        unit.setAddress(vardenhet.getPostadress());
        unit.setCity(vardenhet.getPostort());
        unit.setName(vardenhet.getEnhetsnamn());
        unit.setEmail(vardenhet.getEpost());
        unit.setPhoneNumber(vardenhet.getTelefonnummer());
        unit.setZipCode(vardenhet.getPostnummer());
        unit.setInactive(false);
        return unit;
    }
}
