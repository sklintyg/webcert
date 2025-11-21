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

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.web.privatepractitioner.converter.RegisterPrivatePractitionerConverter;
import se.inera.intyg.webcert.web.privatepractitioner.converter.UpdatePrivatePractitionerConverter;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.CodeDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerResponse;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class PrivatePractitionerService {

    private final WebCertUserService webCertUserService;
    private final PrivatePractitionerIntegrationService privatePractitionerIntegrationService;
    private final RegisterPrivatePractitionerConverter registerPrivatePractitionerConverter;
    private final UpdatePrivatePractitionerConverter updatePrivatePractitionerConverter;
    private final GetUserResourceLinks getUserResourceLinks;

    public void registerPrivatePractitioner(PrivatePractitionerDetails privatePractitionerRegisterRequest) {
        final var user = webCertUserService.getUser();
        
        if (hasNoAccessToRegister(user)) {
            throw new IllegalStateException("User is not authorized to register as private practitioner");
        }

        privatePractitionerIntegrationService.registerPrivatePractitioner(
            registerPrivatePractitionerConverter.convert(privatePractitionerRegisterRequest, webCertUserService)
        );
    }

    public PrivatePractitionerConfigResponse getPrivatePractitionerConfig() {
        final var result = privatePractitionerIntegrationService.getPrivatePractitionerConfig();
        return PrivatePractitionerConfigResponse
            .builder()
            .positions(result.positionCodes().stream().map(position -> new CodeDTO(position.code(), position.description())).toList())
            .healthcareServiceTypes(
                result.healthcareServiceTypeCodes().stream()
                    .map(healthCareServiceType -> new CodeDTO(healthCareServiceType.code(), healthCareServiceType.description()))
                    .toList())
            .typeOfCare(result.typeOfCareCodes()
                .stream()
                .map(typeOfCare -> new CodeDTO(typeOfCare.code(), typeOfCare.description())).toList())
            .build();
    }

    public HospInformationResponse getHospInformation() {
        final var personId = webCertUserService.getUser().getPersonId();
        return HospInformationResponse.convert(privatePractitionerIntegrationService.getHospInformation(personId));
    }

    public PrivatePractitionerResponse getPrivatePractitioner() {
        final var personId = webCertUserService.getUser().getPersonId();
        return PrivatePractitionerResponse.convert(privatePractitionerIntegrationService.getPrivatePractitioner(personId));
    }

    public PrivatePractitionerResponse updatePrivatePractitioner(PrivatePractitionerDetails updatePrivatePractitionerRequest) {
        return PrivatePractitionerResponse.convert(
            privatePractitionerIntegrationService.updatePrivatePractitioner(
                updatePrivatePractitionerConverter.convert(updatePrivatePractitionerRequest, webCertUserService))
        );
    }

    private boolean hasNoAccessToRegister(WebCertUser user) {
        return Arrays.stream(getUserResourceLinks.get(user))
            .noneMatch(link -> (link.getType().equals(ResourceLinkTypeDTO.ACCESS_REGISTER_PRIVATE_PRACTITIONER) && link.isEnabled()));
    }
}
