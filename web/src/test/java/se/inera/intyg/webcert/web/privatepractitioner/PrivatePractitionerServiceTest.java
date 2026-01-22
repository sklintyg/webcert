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
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_REGISTRATION_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_RESPONSE_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_UPDATE_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.PRIVATE_PRACTITIONER_CONFIG_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.kranstegeRequestUpdate;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataIntegration.DR_KRANSTEGE_HOSP_INFO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataIntegration.DR_KRANSTEGE_REGISTRATION_REQUEST;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataIntegration.PRIVATE_PRACTITIONER_CONFIG;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.web.privatepractitioner.factory.RegisterPrivatePractitionerFactory;
import se.inera.intyg.webcert.web.privatepractitioner.factory.UpdatePrivatePractitionerFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerServiceTest {

    @Mock
    WebCertUserService webCertUserService;
    @Mock
    PrivatePractitionerIntegrationService privatePractitionerIntegrationService;
    @Mock
    RegisterPrivatePractitionerFactory registerPrivatePractitionerFactory;
    @Mock
    UpdatePrivatePractitionerFactory updatePrivatePractitionerFactory;
    @Mock
    PrivatePractitionerAccessValidationService privatePractitionerAccessValidationService;
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
    void shouldThrowIfPrivatePractitionerIsNotUnauthorized() {
        mockUser();
        when(privatePractitionerAccessValidationService.hasAccessToRegister(user)).thenReturn(false);
        assertThrows(WebCertServiceException.class, () -> service.registerPrivatePractitioner(DR_KRANSTEGE_REGISTRATION_REQUEST_DTO));
    }

    @Test
    void shouldRegisterPrivatePractitioner() {
        mockUser();
        when(registerPrivatePractitionerFactory.create(DR_KRANSTEGE_REGISTRATION_REQUEST_DTO)).thenReturn(
            DR_KRANSTEGE_REGISTRATION_REQUEST);

        when(privatePractitionerAccessValidationService.hasAccessToRegister(user)).thenReturn(true);

        service.registerPrivatePractitioner(DR_KRANSTEGE_REGISTRATION_REQUEST_DTO);
        verify(privatePractitionerIntegrationService).registerPrivatePractitioner(DR_KRANSTEGE_REGISTRATION_REQUEST);
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
    void shouldReturnLoggedInPrivatePractitioner() {
        mockUser();
        when(privatePractitionerIntegrationService.getPrivatePractitioner(DR_KRANSTEGE_PERSON_ID)).thenReturn(DR_KRANSTEGE);
        final var actual = service.getLoggedInPrivatePractitioner();
        assertEquals(DR_KRANSTEGE_RESPONSE_DTO, actual);
    }

    @Test
    void shouldReturnPrivatePractitioner() {
        when(privatePractitionerIntegrationService.getPrivatePractitioner(DR_KRANSTEGE_HSA_ID)).thenReturn(DR_KRANSTEGE);
        final var actual = service.getPrivatePractitioner(DR_KRANSTEGE_HSA_ID);
        assertEquals(DR_KRANSTEGE_RESPONSE_DTO, actual);
    }

    @Test
    void shouldEditPrivatePractitioner() {
        mockUser();

        when(privatePractitionerAccessValidationService.hasAccessToEdit(user)).thenReturn(true);

        when(updatePrivatePractitionerFactory.create(DR_KRANSTEGE_UPDATE_REQUEST_DTO)).thenReturn(
            kranstegeRequestUpdate().personId(DR_KRANSTEGE_PERSON_ID).build());

        when(privatePractitionerIntegrationService.updatePrivatePractitioner(
            kranstegeRequestUpdate().personId(DR_KRANSTEGE_PERSON_ID).build())).thenReturn(
            DR_KRANSTEGE);

        final var actual = service.editPrivatePractitioner(DR_KRANSTEGE_UPDATE_REQUEST_DTO);

        assertEquals(DR_KRANSTEGE_RESPONSE_DTO, actual);
    }

    @Test
    void shouldThrowIfPrivatePractitionerIsNotAuthorizedToEdit() {
        mockUser();
        when(privatePractitionerAccessValidationService.hasAccessToEdit(user)).thenReturn(false);
        assertThrows(WebCertServiceException.class, () -> service.editPrivatePractitioner(DR_KRANSTEGE_UPDATE_REQUEST_DTO));
    }
}