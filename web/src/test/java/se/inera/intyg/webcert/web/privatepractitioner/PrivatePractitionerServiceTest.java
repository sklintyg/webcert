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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.privatepractitioner.TestData.DR_KRANSTEGE;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_HSA_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PERSON_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_HOSP_INFORMATION_RESPONSE_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_REGISTREATION_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_RESPONSE_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.PRIVATE_PRACTITIONER_CONFIG_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataIntegration.DR_KRANSTEGE_HOSP_INFO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataIntegration.DR_KRANSTEGE_REGISTREATION_REQUEST;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataIntegration.PRIVATE_PRACTITIONER_CONFIG;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerServiceTest {

    @Mock
    WebCertUserService webCertUserService;
    @Mock
    PrivatePractitionerIntegrationService privatePractitionerIntegrationService;
    @Mock
    GetUserResourceLinks getUserResourceLinks;
    @InjectMocks
    PrivatePractitionerService service;

    WebCertUser user;

    void mockUser() {
        user = new WebCertUser();
        user.setPersonId(DR_KRANSTEGE_PERSON_ID);
        user.setNamn(DR_KRANSTEGE_NAME);
        user.setHsaId(DR_KRANSTEGE_HSA_ID);
        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Test
    void shouldThrowIfPrivatePractitionerIdNotUnauthorized() {
        mockUser();
        when(getUserResourceLinks.get(user)).thenReturn(new ResourceLinkDTO[]{ResourceLinkDTO.create(ResourceLinkTypeDTO.ACCESS_SIGNED_CERTIFICATES_LIST, "", "", true)});
        assertThrows(IllegalStateException.class, () -> service.registerPrivatePractitioner(DR_KRANSTEGE_REGISTREATION_REQUEST_DTO));
    }

    @Test
    void shouldRegisterPrivatePractitioner() {
        mockUser();
        when(getUserResourceLinks.get(user)).thenReturn(new ResourceLinkDTO[]{ResourceLinkDTO.create(ResourceLinkTypeDTO.ACCESS_REGISTER_PRIVATE_PRACTITIONER, "", "", true)});
        service.registerPrivatePractitioner(DR_KRANSTEGE_REGISTREATION_REQUEST_DTO);
        verify(privatePractitionerIntegrationService).registerPrivatePractitioner(DR_KRANSTEGE_REGISTREATION_REQUEST);
    }

    @Test
    void shouldReturnConfigResponse() {
        when(privatePractitionerIntegrationService.getPrivatePractitionerConfig()).thenReturn(PRIVATE_PRACTITIONER_CONFIG);
        PrivatePractitionerConfigResponse result = service.getPrivatePractitionerConfig();
        assertEquals(PRIVATE_PRACTITIONER_CONFIG_DTO, result);
    }

    @Test
    void shouldReturnHospInformation() {
        mockUser();
        when(privatePractitionerIntegrationService.getHospInformation(DR_KRANSTEGE_PERSON_ID)).thenReturn(DR_KRANSTEGE_HOSP_INFO);
        final var result = service.getHospInformation();
        assertEquals(DR_KRANSTEGE_HOSP_INFORMATION_RESPONSE_DTO, result);
    }

    @Test
    void shouldReturnPrivatePractitioner() {
        mockUser();
        when(privatePractitionerIntegrationService.getPrivatePractitioner(DR_KRANSTEGE_PERSON_ID)).thenReturn(DR_KRANSTEGE);
        final var actual = service.getPrivatePractitioner();
        assertEquals(DR_KRANSTEGE_RESPONSE_DTO, actual);
    }
}