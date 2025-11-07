package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import model.ValidatePrivatePractitionerRequest;
import model.ValidatePrivatePractitionerResponse;
import model.ValidatePrivatePractitionerResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerServiceImplTest {

  public static final String PERSONAL_IDENTITY_NUMBER = "191212121212";
  @Mock
  private RestClient ppRestClient;

  private PrivatePractitionerServiceImpl service;

  private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  private RestClient.ResponseSpec responseSpec;

  @Captor
  private ArgumentCaptor<ValidatePrivatePractitionerRequest> requestCaptor;

  @BeforeEach
  void setUp() {
    service = new PrivatePractitionerServiceImpl(ppRestClient);
  }

  private void mockPostChain() {
    requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    responseSpec = mock(RestClient.ResponseSpec.class);
    when(ppRestClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.body(any(ValidatePrivatePractitionerRequest.class))).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void validatePrivatePractitionerReturnsResponseOnOk() {
    mockPostChain();
    var expectedResponse = new ValidatePrivatePractitionerResponse();
    expectedResponse.setResultCode(ValidatePrivatePractitionerResultCode.OK);
    expectedResponse.setResultText("OK");
    when(responseSpec.body(eq(ValidatePrivatePractitionerResponse.class))).thenReturn(expectedResponse);

    var actual = service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);

    assertNotNull(actual);
    assertEquals(ValidatePrivatePractitionerResultCode.OK, actual.getResultCode());
    assertEquals("OK", actual.getResultText());

    verify(ppRestClient).post();
    verify(requestBodyUriSpec).contentType(MediaType.APPLICATION_JSON);
    verify(requestBodyUriSpec).body(requestCaptor.capture());
    assertEquals(PERSONAL_IDENTITY_NUMBER, requestCaptor.getValue().getPersonalIdentityNumber());
  }

  @Test
  void validatePrivatePractitionerInvalid() {
    mockPostChain();
    var expectedResponse = new ValidatePrivatePractitionerResponse();
    expectedResponse.setResultCode(ValidatePrivatePractitionerResultCode.NO_ACCOUNT);
    expectedResponse.setResultText("No account found for practitioner");
    when(responseSpec.body(eq(ValidatePrivatePractitionerResponse.class))).thenReturn(expectedResponse);

    var actual = service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER);

    assertNotNull(actual);
    assertEquals(ValidatePrivatePractitionerResultCode.NO_ACCOUNT, actual.getResultCode());
    assertEquals("No account found for practitioner", actual.getResultText());

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
    mockPostChain();
    when(responseSpec.body(eq(ValidatePrivatePractitionerResponse.class))).thenReturn(null);
    assertThrows(RestClientException.class, () -> service.validatePrivatePractitioner(PERSONAL_IDENTITY_NUMBER));
  }
}