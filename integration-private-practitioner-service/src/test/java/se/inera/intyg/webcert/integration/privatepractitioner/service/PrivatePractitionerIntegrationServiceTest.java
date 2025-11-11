package se.inera.intyg.webcert.integration.privatepractitioner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataDTO.kranstegeRegisterPractitionerRequest;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataModel.DR_KRANSTEGE;
import static se.inera.intyg.webcert.logging.MdcHelper.LOG_SESSION_ID_HEADER;
import static se.inera.intyg.webcert.logging.MdcHelper.LOG_TRACE_ID_HEADER;
import static se.inera.intyg.webcert.logging.MdcLogConstants.SESSION_ID_KEY;
import static se.inera.intyg.webcert.logging.MdcLogConstants.TRACE_ID_KEY;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerResultCode;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerIntegrationServiceTest {

    public static final String PERSONAL_IDENTITY_NUMBER = "191212121212";
    @Mock
    private RestClient ppRestClient;
    @Mock
    private RegisterPrivatePractitionerClient registerPrivatePractitionerClient;
    @InjectMocks
    private PrivatePractitionerIntegrationService service;

    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    private RestClient.ResponseSpec responseSpec;

    @Captor
    private ArgumentCaptor<ValidatePrivatePractitionerRequest> requestCaptor;

    private void mockPostChain(String uri) {
        MDC.put(TRACE_ID_KEY, "traceId");
        MDC.put(SESSION_ID_KEY, "sessionId");

        requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        when(ppRestClient.post()).thenReturn(requestBodyUriSpec);

        if (uri != null) {
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
        }
        when(requestBodyUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(LOG_TRACE_ID_HEADER, "traceId")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(LOG_SESSION_ID_HEADER, "sessionId")).thenReturn(
            requestBodyUriSpec);

        when(requestBodyUriSpec.body(any(ValidatePrivatePractitionerRequest.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void validatePrivatePractitionerReturnsResponseOnOk() {
        mockPostChain("/validate");
        var expectedResponse = new ValidatePrivatePractitionerResponse(ValidatePrivatePractitionerResultCode.OK, "OK");
        when(responseSpec.body(ValidatePrivatePractitionerResponse.class)).thenReturn(expectedResponse);

        var actual = service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);

        assertNotNull(actual);
        assertEquals(ValidatePrivatePractitionerResultCode.OK, actual.resultCode());
        assertEquals("OK", actual.resultText());

        verify(ppRestClient).post();
        verify(requestBodyUriSpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodyUriSpec).body(requestCaptor.capture());
        assertEquals(PERSONAL_IDENTITY_NUMBER, requestCaptor.getValue().getPersonalIdentityNumber());
    }

    @Test
    void validatePrivatePractitionerInvalid() {
        mockPostChain("/validate");
        var expectedResponse = new ValidatePrivatePractitionerResponse(ValidatePrivatePractitionerResultCode.NO_ACCOUNT,
            "No account found for practitioner");
        when(responseSpec.body(ValidatePrivatePractitionerResponse.class)).thenReturn(expectedResponse);

        var actual = service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);

        assertNotNull(actual);
        assertEquals(ValidatePrivatePractitionerResultCode.NO_ACCOUNT, actual.resultCode());
        assertEquals("No account found for practitioner", actual.resultText());

        verify(ppRestClient).post();
        verify(requestBodyUriSpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodyUriSpec).body(any(ValidatePrivatePractitionerRequest.class));
    }

    @Test
    void validatePrivatePractitionerThrowsOnEmptyIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> service.validatePrivatePractitioner(""));
        assertThrows(IllegalArgumentException.class, () -> service.validatePrivatePractitioner(null));
        verifyNoInteractions(ppRestClient);
    }

    @Test
    void validatePrivatePractitionerThrowsWhenResponseNull() {
        mockPostChain("/validate");
        when(responseSpec.body(ValidatePrivatePractitionerResponse.class)).thenReturn(null);
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