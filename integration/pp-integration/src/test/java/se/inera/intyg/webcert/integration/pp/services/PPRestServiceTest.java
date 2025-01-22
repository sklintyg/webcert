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
package se.inera.intyg.webcert.integration.pp.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerRequest;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResponse;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode;

@RunWith(MockitoJUnitRunner.class)
public class PPRestServiceTest {

    private final static String PERSONAL_IDENTITY_NUMBER = "19121212-1212";
    private final static String VALIDATE_PP_REST_URL = "ppUrl/validate";

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    PPRestServiceImpl ppRestService = new PPRestServiceImpl();

    @Before
    public void init() {
        ReflectionTestUtils.setField(ppRestService, "internalApiValidatePrivatePractitionerUrl", VALIDATE_PP_REST_URL);
    }

    @Test
    public void testValidatePrivatePractitionerValidOk() {
        final var request = new ValidatePrivatePractitionerRequest(PERSONAL_IDENTITY_NUMBER);
        when(restTemplate.postForObject(VALIDATE_PP_REST_URL, request, ValidatePrivatePractitionerResponse.class))
            .thenReturn(createReponse(ValidatePrivatePractitionerResultCode.OK));

        final var response = ppRestService.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);
        assertEquals(ValidatePrivatePractitionerResultCode.OK, response.getResultCode());
    }

    @Test
    public void testValidatePrivatePractitionerInvalid() {
        final var request = new ValidatePrivatePractitionerRequest(PERSONAL_IDENTITY_NUMBER);
        when(restTemplate.postForObject(VALIDATE_PP_REST_URL, request, ValidatePrivatePractitionerResponse.class))
            .thenReturn(createReponse(ValidatePrivatePractitionerResultCode.NO_ACCOUNT));

        final var response = ppRestService.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);
        assertEquals(ValidatePrivatePractitionerResultCode.NO_ACCOUNT, response.getResultCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidatePrivatePractitionerWhenPersonalIdentityNumberIsNull() {
        ppRestService.validatePrivatePractitioner(null);
    }

    @Test(expected = RestClientException.class)
    public void testValidatePrivatePractitionerWhenRestCallFails() {
        final var request = new ValidatePrivatePractitionerRequest(PERSONAL_IDENTITY_NUMBER);
        when(restTemplate.postForObject(VALIDATE_PP_REST_URL, request, ValidatePrivatePractitionerResponse.class))
            .thenReturn(null);

        ppRestService.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);
    }

    private ValidatePrivatePractitionerResponse createReponse(ValidatePrivatePractitionerResultCode validatePrivatePractitionerResultCode) {
        final var response = new ValidatePrivatePractitionerResponse();
        response.setResultCode(validatePrivatePractitionerResultCode);
        response.setResultText("Result text");
        return response;
    }

}
