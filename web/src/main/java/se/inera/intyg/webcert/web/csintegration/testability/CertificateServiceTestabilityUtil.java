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
        final var certificateRequestDTO = new CreateCertificateRequestDTO();
        final var careUnit = certificateServiceCreateRequest.getHosPerson().getVardenhet();
        certificateRequestDTO.setUser(certificateServiceUserBuilder.build(certificateServiceCreateRequest.getHosPerson()));
        certificateRequestDTO.setPatient(certificateServicePatientHelper.get(certificateServiceCreateRequest.getPatient().getPersonId()));
        certificateRequestDTO.setCertificateModelId(certificateServiceCreateRequest.getCertificateModelId());
        certificateRequestDTO.setCareProvider(convertToCareProvider(careUnit.getVardgivare()));
        certificateRequestDTO.setCareUnit(convertToUnit(careUnit));
        certificateRequestDTO.setUnit(convertToUnit(careUnit));
        return certificateRequestDTO;
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
