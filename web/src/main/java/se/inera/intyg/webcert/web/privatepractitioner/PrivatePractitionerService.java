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

package se.inera.intyg.webcert.web.privatepractitioner;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.privatepractitioner.model.OwnershipType;
import se.inera.intyg.webcert.integration.privatepractitioner.model.RegisterPrivatePractitionerRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HealthCareServiceTypeDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PositionDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConsentDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerRegistrationRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.TypeOfCareDTO;

@Service
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class PrivatePractitionerService {

    private final WebCertUserService webCertUserService;
    private final PrivatePractitionerIntegrationService privatePractitionerIntegrationService;

    public PrivatePractitionerDTO registerPrivatePractitioner(
        PrivatePractitionerRegistrationRequest privatePractitionerRegisterRequest) {
        final var user = webCertUserService.getUser();
        return PrivatePractitionerDTO.create(
            privatePractitionerIntegrationService.registerPrivatePractitioner(RegisterPrivatePractitionerRequest
                .builder()
                .personId(user.getPersonId())
                .name(user.getName())
                .position(privatePractitionerRegisterRequest.getPosition())
                .careUnitName(privatePractitionerRegisterRequest.getCareUnitName())
                .ownershipType(OwnershipType.PRIVATE.getValue())
                .typeOfCare(privatePractitionerRegisterRequest.getTypeOfCare())
                .healthcareServiceType(privatePractitionerRegisterRequest.getHealthcareServiceType())
                .workplaceCode(privatePractitionerRegisterRequest.getWorkplaceCode())
                .phoneNumber(privatePractitionerRegisterRequest.getPhoneNumber())
                .email(privatePractitionerRegisterRequest.getEmail())
                .address(privatePractitionerRegisterRequest.getAddress())
                .zipCode(privatePractitionerRegisterRequest.getZipCode())
                .municipality(privatePractitionerRegisterRequest.getMunicipality())
                .county(privatePractitionerRegisterRequest.getCounty())
                .consentFormVersion(privatePractitionerRegisterRequest.getConsentFormVersion())
                .build()));
    }


    public PrivatePractitionerDTO getPrivatePractitioner() {
        return null;
    }

    public void updatePrivatePractitioner(PrivatePractitionerDTO privatePractitioner) {
    }

    public PrivatePractitionerConfigResponse getPrivatePractitionerConfig() {
        final var result = privatePractitionerIntegrationService.getPrivatePractitionerConfig();
        return PrivatePractitionerConfigResponse
            .builder()
            .positions(result.getPositions().stream().map(position -> new PositionDTO(position.code(), position.name())).toList())
            .healthcareServiceType(
                result.getHealthCareServiceTypes().stream()
                    .map(healthCareServiceType -> new HealthCareServiceTypeDTO(healthCareServiceType.code(), healthCareServiceType.name()))
                    .toList())
            .typeOfCare(result.getTypeOfCare().stream().map(typeOfCare -> new TypeOfCareDTO(typeOfCare.code(), typeOfCare.name())).toList())
            .consent(new PrivatePractitionerConsentDTO(result.getConsent().content(), result.getConsent().version()))
            .build();
    }


    public HospInformationResponse getHospInformation() {
        final var hsaId = webCertUserService.getUser().getHsaId();
        return HospInformationResponse.convert(privatePractitionerIntegrationService.getHospInformation(hsaId));
    }

}
