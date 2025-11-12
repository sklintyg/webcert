package se.inera.intyg.webcert.integration.privatepractitioner.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
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
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitioner;
import se.inera.intyg.webcert.integration.privatepractitioner.model.RegisterPrivatePractitionerRequest;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;

@ExtendWith(MockitoExtension.class)
class RegisterPrivatePractitionerClientTest {

  private static final String SESSION_ID = "session-123";
  private static final String TRACE_ID = "trace-456";
  private static final String PERSON_ID = "191212121212";
  private static final String NAME = "Test Testsson";
  private static final String HSA_ID = "HSA-123456";
  private static final String POSITION = "LK";
  private static final String CARE_UNIT_NAME = "Test Vårdcental";
  private static final String CARE_PROVIDER_NAME = "Test Vårdgivare";

  @Mock
  private RestClient ppsRestClient;

  @InjectMocks
  private RegisterPrivatePractitionerClient registerPrivatePractitionerClient;

  @Captor
  private ArgumentCaptor<RegisterPrivatePractitionerRequest> requestCaptor;

  private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  private RestClient.ResponseSpec responseSpec;

  @BeforeEach
  void setUp() {
    MDC.put(MdcLogConstants.SESSION_ID_KEY, SESSION_ID);
    MDC.put(MdcLogConstants.TRACE_ID_KEY, TRACE_ID);

    requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    responseSpec = mock(RestClient.ResponseSpec.class);

    when(ppsRestClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.body(any(RegisterPrivatePractitionerRequest.class))).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void shouldRegisterPrivatePractitioner() {
    final var registrationRequest = RegisterPrivatePractitionerRequest.builder()
        .personId(PERSON_ID)
        .name(NAME)
        .position(POSITION)
        .careUnitName(CARE_UNIT_NAME)
        .ownershipType("Privat")
        .typeOfCare("OPEN")
        .healthcareServiceType("01")
        .workplaceCode("1234567890")
        .phoneNumber("0701234567")
        .email("test@test.se")
        .address("Testgatan 1")
        .zipCode("12345")
        .city("Teststad")
        .municipality("Testkommun")
        .county("Testlän")
        .consentFormVersion(1L)
        .build();

    final var expectedResponse = PrivatePractitioner.builder()
        .hsaId(HSA_ID)
        .personId(PERSON_ID)
        .name(NAME)
        .position(POSITION)
        .careUnitName(CARE_UNIT_NAME)
        .careProviderName(CARE_PROVIDER_NAME)
        .ownershipType("Privat")
        .typeOfCare("OPEN")
        .healthcareServiceType("01")
        .workplaceCode("1234567890")
        .phoneNumber("0701234567")
        .email("test@test.se")
        .address("Testgatan 1")
        .zipCode("12345")
        .city("Teststad")
        .municipality("Testkommun")
        .county("Testlän")
        .consentFormVersion(1L)
        .registrationDate(LocalDateTime.now())
        .build();

    when(responseSpec.body(PrivatePractitioner.class)).thenReturn(expectedResponse);

    final var result = registerPrivatePractitionerClient.registerPrivatePractitioner(registrationRequest);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(HSA_ID, result.getHsaId()),
        () -> assertEquals(PERSON_ID, result.getPersonId()),
        () -> assertEquals(NAME, result.getName()),
        () -> assertEquals(POSITION, result.getPosition()),
        () -> assertEquals(CARE_UNIT_NAME, result.getCareUnitName()),
        () -> assertEquals(CARE_PROVIDER_NAME, result.getCareProviderName()),
        () -> assertNotNull(result.getRegistrationDate())
    );
  }

  @Test
  void shouldSetCorrectHeaders() {
    final var registrationRequest = RegisterPrivatePractitionerRequest.builder()
        .personId(PERSON_ID)
        .name(NAME)
        .consentFormVersion(1L)
        .build();

    final var expectedResponse = PrivatePractitioner.builder()
        .hsaId(HSA_ID)
        .personId(PERSON_ID)
        .name(NAME)
        .build();

    when(responseSpec.body(PrivatePractitioner.class)).thenReturn(expectedResponse);

    registerPrivatePractitionerClient.registerPrivatePractitioner(registrationRequest);

    verify(requestBodyUriSpec).header(MdcHelper.LOG_SESSION_ID_HEADER, SESSION_ID);
    verify(requestBodyUriSpec).header(MdcHelper.LOG_TRACE_ID_HEADER, TRACE_ID);
  }

  @Test
  void shouldSendCorrectRequestBody() {
    final var registrationRequest = RegisterPrivatePractitionerRequest.builder()
        .personId(PERSON_ID)
        .name(NAME)
        .position(POSITION)
        .careUnitName(CARE_UNIT_NAME)
        .consentFormVersion(1L)
        .build();

    final var expectedResponse = PrivatePractitioner.builder()
        .hsaId(HSA_ID)
        .personId(PERSON_ID)
        .name(NAME)
        .build();

    when(responseSpec.body(PrivatePractitioner.class)).thenReturn(expectedResponse);

    registerPrivatePractitionerClient.registerPrivatePractitioner(registrationRequest);

    verify(requestBodyUriSpec).body(requestCaptor.capture());
    final var capturedRequest = requestCaptor.getValue();

    assertAll(
        () -> assertEquals(PERSON_ID, capturedRequest.getPersonId()),
        () -> assertEquals(NAME, capturedRequest.getName()),
        () -> assertEquals(POSITION, capturedRequest.getPosition()),
        () -> assertEquals(CARE_UNIT_NAME, capturedRequest.getCareUnitName()),
        () -> assertEquals(1L, capturedRequest.getConsentFormVersion())
    );
  }
}