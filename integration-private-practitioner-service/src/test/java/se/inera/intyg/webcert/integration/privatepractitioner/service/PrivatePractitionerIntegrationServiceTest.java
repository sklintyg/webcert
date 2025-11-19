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

package se.inera.intyg.webcert.integration.privatepractitioner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.testdata.TestData.DR_KRANSTEGE;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.testdata.TestData.DR_KRANSTEGE_HOSP_INFO;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.testdata.TestData.PRIVATE_PRACTITIONER_CONFIG;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.testdata.TestData.kranstegeRegisterPractitionerRequest;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.testdata.TestDataConstants.DR_KRANSTEGE_PERSON_ID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitioner;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationResultCode;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerIntegrationServiceTest {

    @Mock
    private PPSIntegrationService ppsIntegrationService;
    @InjectMocks
    private PrivatePractitionerIntegrationService service;

    @Test
    void validatePrivatePractitionerReturnsResponseOnOk() {
        final var expectedResponse = new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.OK, "OK");
        when(ppsIntegrationService.validatePrivatePractitioner(new PrivatePractitionerValidationRequest(DR_KRANSTEGE_PERSON_ID)))
            .thenReturn(expectedResponse);

        final var actual = service.validatePrivatePractitioner(DR_KRANSTEGE_PERSON_ID);

        assertEquals(expectedResponse, actual);
    }

    @Test
    void validatePrivatePractitionerInvalid() {
        final var expectedResponse = new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.NO_ACCOUNT,
            "No account found for practitioner");
        when(ppsIntegrationService.validatePrivatePractitioner(new PrivatePractitionerValidationRequest(DR_KRANSTEGE_PERSON_ID)))
            .thenReturn(expectedResponse);

        final var actual = service.validatePrivatePractitioner(DR_KRANSTEGE_PERSON_ID);

        assertEquals(expectedResponse, actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validatePrivatePractitionerThrowsOnEmptyIdentifier(String id) {
        assertThrows(IllegalArgumentException.class, () -> service.validatePrivatePractitioner(id));
    }

    @Test
    void validatePrivatePractitionerThrowsWhenResponseNull() {
        when(ppsIntegrationService.validatePrivatePractitioner(any())).thenReturn(null);
        assertThrows(RestClientException.class, () -> service.validatePrivatePractitioner(DR_KRANSTEGE_PERSON_ID));
    }

    @Test
    void shallReturnRegisteredPrivatePractitioner() {
        when(ppsIntegrationService.registerPrivatePractitioner(kranstegeRegisterPractitionerRequest())).thenReturn(
            DR_KRANSTEGE);
        final var result = service.registerPrivatePractitioner(kranstegeRegisterPractitionerRequest());

        assertEquals(DR_KRANSTEGE, result);
    }

    @Test
    void shallReturnHospInformation() {
        when(ppsIntegrationService.getHospInformation(any())).thenReturn(DR_KRANSTEGE_HOSP_INFO);
        final var result = service.getHospInformation(DR_KRANSTEGE_PERSON_ID);
        assertEquals(DR_KRANSTEGE_HOSP_INFO, result);
    }

    @Test
    void shallReturnPrivatePractitionerConfig() {
        when(ppsIntegrationService.getPrivatePractitionerConfig()).thenReturn(PRIVATE_PRACTITIONER_CONFIG);
        final var result = service.getPrivatePractitionerConfig();
        assertEquals(PRIVATE_PRACTITIONER_CONFIG, result);
    }

    @Test
    void shallReturnPrivatePractitioner() {
        when(ppsIntegrationService.getPrivatePractitioner(DR_KRANSTEGE_PERSON_ID)).thenReturn(DR_KRANSTEGE);
        final var result = service.getPrivatePractitioner(DR_KRANSTEGE_PERSON_ID);
        assertEquals(DR_KRANSTEGE, result);
    }

    @Test
    void shallUpdateRegisteredPrivatePractitioner() {
        final var result = service.updatePrivatePractitioner(kranstegeRegisterPractitionerRequest());

        assertEquals(PrivatePractitioner.builder().build(), result);
    }
}