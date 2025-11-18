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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerValidationRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerValidationResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerValidationResultCode;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerIntegrationServiceTest {

    public static final String PERSONAL_IDENTITY_NUMBER = "191212121212";
    @Mock
    private RegisterPrivatePractitionerClient registerPrivatePractitionerClient;
    @Mock
    private ValidatePrivatePractitionerClient validatePrivatePractitionerClient;
    @Mock
    private GetHospInformationClient getHospInformationClient;
    @Mock
    private GetPrivatePractitionerConfigurationClient getPrivatePractitionerConfigurationClient;
    @InjectMocks
    private PrivatePractitionerIntegrationService service;


    @Captor
    private ArgumentCaptor<PrivatePractitionerValidationRequest> requestCaptor;


    @Test
    void validatePrivatePractitionerReturnsResponseOnOk() {
        var expectedResponse = new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.OK, "OK");
        when(validatePrivatePractitionerClient.validatePrivatePractitioner(
            new PrivatePractitionerValidationRequest(PERSONAL_IDENTITY_NUMBER))).thenReturn(expectedResponse);

        var actual = service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);

        assertEquals(expectedResponse, actual);

    }

    @Test
    void validatePrivatePractitionerInvalid() {
        var expectedResponse = new PrivatePractitionerValidationResponse(PrivatePractitionerValidationResultCode.NO_ACCOUNT,
            "No account found for practitioner");
        when(validatePrivatePractitionerClient.validatePrivatePractitioner(
            new PrivatePractitionerValidationRequest(PERSONAL_IDENTITY_NUMBER))).thenReturn(expectedResponse);

        var actual = service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);

        assertEquals(expectedResponse, actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validatePrivatePractitionerThrowsOnEmptyIdentifier(String id) {
        assertThrows(IllegalArgumentException.class, () -> service.validatePrivatePractitioner(id));
    }

    @Test
    void validatePrivatePractitionerThrowsWhenResponseNull() {
        when(validatePrivatePractitionerClient.validatePrivatePractitioner(any())).thenReturn(null);
        assertThrows(RestClientException.class, () -> service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER));
    }

    @Test
    void shallReturnRegisteredPrivatePractitioner() {
        when(registerPrivatePractitionerClient.registerPrivatePractitioner(kranstegeRegisterPractitionerRequest())).thenReturn(
            DR_KRANSTEGE);
        final var result = service.registerPrivatePractitioner(kranstegeRegisterPractitionerRequest());

        assertEquals(DR_KRANSTEGE, result);
    }

    @Test
    void shallReturnHospInformation() {
        when(getHospInformationClient.getHospInformation(any())).thenReturn(DR_KRANSTEGE_HOSP_INFO);
        final var result = service.getHospInformation(PERSONAL_IDENTITY_NUMBER);
        assertEquals(DR_KRANSTEGE_HOSP_INFO, result);
    }

    @Test
    void shallReturnPrivatePractitionerConfig() {
        when(getPrivatePractitionerConfigurationClient.getPrivatePractitionerConfig()).thenReturn(PRIVATE_PRACTITIONER_CONFIG);
        final var result = service.getPrivatePractitionerConfig();
        assertEquals(PRIVATE_PRACTITIONER_CONFIG, result);
    }
}