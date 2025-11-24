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

package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_HOSP_INFORMATION_RESPONSE_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_REGISTRATION_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_RESPONSE_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_UPDATE_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.PRIVATE_PRACTITIONER_CONFIG_DTO;

import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerApiControllerTest {

    @Mock
    PrivatePractitionerService service;
    @InjectMocks
    PrivatePractitionerApiController controller;

    @Test
    void shouldRegisterPractitioner() {
        final var actual = controller.registerPractitioner(DR_KRANSTEGE_REGISTRATION_REQUEST_DTO);

        verify(service).registerPrivatePractitioner(DR_KRANSTEGE_REGISTRATION_REQUEST_DTO);
        assertEquals(Status.OK, actual.getStatusInfo().toEnum());
    }

    @Test
    void shouldGetPrivatePractitioner() {
        when(service.getPrivatePractitioner()).thenReturn(DR_KRANSTEGE_RESPONSE_DTO);

        final var actual = controller.getPrivatePractitioner();
        assertEquals(DR_KRANSTEGE_RESPONSE_DTO, actual);
    }

    @Test
    void shouldGetPrivatePractitionerConfig() {
        when(service.getPrivatePractitionerConfig()).thenReturn(PRIVATE_PRACTITIONER_CONFIG_DTO);

        final var actual = controller.getPrivatePractitionerConfig();
        assertEquals(PRIVATE_PRACTITIONER_CONFIG_DTO, actual);
    }

    @Test
    void shouldGetHospInformation() {
        when(service.getHospInformation()).thenReturn(DR_KRANSTEGE_HOSP_INFORMATION_RESPONSE_DTO);

        final var actual = controller.getHospInformation();

        assertEquals(DR_KRANSTEGE_HOSP_INFORMATION_RESPONSE_DTO, actual);
    }

    @Test
    void shouldUpdatePrivatePractitioner() {
        when(service.editPrivatePractitioner(DR_KRANSTEGE_UPDATE_REQUEST_DTO)).thenReturn(DR_KRANSTEGE_RESPONSE_DTO);

        final var actual = controller.updatePrivatePractitioner(DR_KRANSTEGE_UPDATE_REQUEST_DTO);

        assertEquals(DR_KRANSTEGE_RESPONSE_DTO, actual);
    }
}

