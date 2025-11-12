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
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.CodeDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerRegistrationRequest;

@Service
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class PrivatePractitionerService {

    private final WebCertUserService webCertUserService;
    private final PrivatePractitionerIntegrationService privatePractitionerIntegrationService;

    public void registerPrivatePractitioner(
        PrivatePractitionerRegistrationRequest privatePractitionerRegisterRequest) {
        final var user = webCertUserService.getUser();

        privatePractitionerIntegrationService.registerPrivatePractitioner(RegisterPrivatePractitionerRequest
            .builder()
            .personId(user.getPersonId())
            .name(user.getNamn())
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
            .city(privatePractitionerRegisterRequest.getCity())
            .municipality(privatePractitionerRegisterRequest.getMunicipality())
            .county(privatePractitionerRegisterRequest.getCounty())
            .build());
    }

    public PrivatePractitionerConfigResponse getPrivatePractitionerConfig() {
        final var result = privatePractitionerIntegrationService.getPrivatePractitionerConfig();
        return PrivatePractitionerConfigResponse
            .builder()
            .positions(result.getPositionCodes().stream().map(position -> new CodeDTO(position.code(), position.description())).toList())
            .healthcareServiceTypes(
                result.getHealthcareServiceTypeCodes().stream()
                    .map(healthCareServiceType -> new CodeDTO(healthCareServiceType.code(), healthCareServiceType.description()))
                    .toList())
            .typeOfCare(result.getTypeOfCareCodes()
                .stream()
                .map(typeOfCare -> new CodeDTO(typeOfCare.code(), typeOfCare.description())).toList())
            .build();
    }


    public HospInformationResponse getHospInformation() {
        final var personId = webCertUserService.getUser().getPersonId();
        return HospInformationResponse.convert(privatePractitionerIntegrationService.getHospInformation(personId));
    }

}
