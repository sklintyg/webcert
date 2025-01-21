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

package se.inera.intyg.webcert.web.csintegration.testability;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;

@Component
public class CertificateServiceTestabilityUtil {

    private final CertificateServicePatientHelper certificateServicePatientHelper;
    private final CSTestabilityIntegrationService csTestabilityIntegrationService;
    private final CertificateServiceUserBuilder certificateServiceUserBuilder;

    public CertificateServiceTestabilityUtil(CertificateServicePatientHelper certificateServicePatientHelper,
        CSTestabilityIntegrationService csTestabilityIntegrationService, CertificateServiceUserBuilder certificateServiceUserBuilder) {
        this.certificateServicePatientHelper = certificateServicePatientHelper;
        this.csTestabilityIntegrationService = csTestabilityIntegrationService;
        this.certificateServiceUserBuilder = certificateServiceUserBuilder;
    }

    public String create(CertificateServiceCreateRequest certificateServiceCreateRequest) {
        final var certificateRequestDTO = createRequest(certificateServiceCreateRequest);
        final var certificate = csTestabilityIntegrationService.createCertificate(certificateRequestDTO);
        return certificate.getMetadata().getId();
    }

    private CreateCertificateRequestDTO createRequest(CertificateServiceCreateRequest certificateServiceCreateRequest) {
        final var careUnit = certificateServiceCreateRequest.getHosPerson().getVardenhet();
        return CreateCertificateRequestDTO.builder()
            .user(certificateServiceUserBuilder.build(certificateServiceCreateRequest.getHosPerson()))
            .patient(certificateServicePatientHelper.get(certificateServiceCreateRequest.getPatient().getPersonId()))
            .unit(convertToUnit(careUnit))
            .careUnit(convertToUnit(careUnit))
            .careProvider(convertToCareProvider(careUnit.getVardgivare()))
            .certificateModelId(certificateServiceCreateRequest.getCertificateModelId())
            .fillType(certificateServiceCreateRequest.getFillType())
            .status(certificateServiceCreateRequest.getStatus())
            .build();
    }

    private static CertificateServiceUnitDTO convertToCareProvider(Vardgivare vardgivare) {
        return CertificateServiceUnitDTO.builder()
            .id(vardgivare.getVardgivarid())
            .name(vardgivare.getVardgivarnamn())
            .build();
    }

    private static CertificateServiceUnitDTO convertToUnit(Vardenhet vardenhet) {
        return CertificateServiceUnitDTO.builder()
            .id(vardenhet.getEnhetsid())
            .address(vardenhet.getPostadress())
            .city(vardenhet.getPostort())
            .name(vardenhet.getEnhetsnamn())
            .email(vardenhet.getEpost())
            .phoneNumber(vardenhet.getTelefonnummer())
            .zipCode(vardenhet.getPostnummer())
            .workplaceCode(vardenhet.getArbetsplatsKod())
            .inactive(false)
            .build();
    }
}
