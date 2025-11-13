package se.inera.intyg.webcert.integration.privatepractitioner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.testdata.TestData.DR_KRANSTEGE;
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
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerResultCode;

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
    private ArgumentCaptor<ValidatePrivatePractitionerRequest> requestCaptor;


    @Test
    void validatePrivatePractitionerReturnsResponseOnOk() {
        var expectedResponse = new ValidatePrivatePractitionerResponse(ValidatePrivatePractitionerResultCode.OK, "OK");
        when(validatePrivatePractitionerClient.validatePrivatePractitioner(
            new ValidatePrivatePractitionerRequest(PERSONAL_IDENTITY_NUMBER))).thenReturn(expectedResponse);

        var actual = service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);

        assertEquals(expectedResponse, actual);

    }

    @Test
    void validatePrivatePractitionerInvalid() {
        var expectedResponse = new ValidatePrivatePractitionerResponse(ValidatePrivatePractitionerResultCode.NO_ACCOUNT,
            "No account found for practitioner");
        when(validatePrivatePractitionerClient.validatePrivatePractitioner(
            new ValidatePrivatePractitionerRequest(PERSONAL_IDENTITY_NUMBER))).thenReturn(expectedResponse);

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
}