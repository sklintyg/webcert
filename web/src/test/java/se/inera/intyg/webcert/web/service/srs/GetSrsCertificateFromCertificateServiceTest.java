package se.inera.intyg.webcert.web.service.srs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetSickLeaveCertificateInternalResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SickLeaveCertificateDTO;

@ExtendWith(MockitoExtension.class)
class GetSrsCertificateFromCertificateServiceTest {

  @Mock
  private CSIntegrationService csIntegrationService;

  private GetSrsCertificateFromCertificateService getSrsCertificateFromCertificateService;

  @BeforeEach
  void setUp() {
    getSrsCertificateFromCertificateService = new GetSrsCertificateFromCertificateService(csIntegrationService);
  }

  @Test
  void shouldReturnNullWhenCsIntegrationServiceReturnsEmptyOptional() {
    when(csIntegrationService.getSickLeaveCertificate("cert-1")).thenReturn(Optional.empty());

    final var result = getSrsCertificateFromCertificateService.getSrsCertificate("cert-1");

    assertNull(result);
    verify(csIntegrationService).getSickLeaveCertificate("cert-1");
  }

  @Test
  void shouldReturnNullWhenCertificateIsNotAvailable() {
    final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
        .available(false)
        .sickLeaveCertificate(SickLeaveCertificateDTO.builder()
            .id("cert-1")
            .diagnoseCode("F438A")
            .build())
        .build();

    when(csIntegrationService.getSickLeaveCertificate("cert-1")).thenReturn(Optional.of(response));

    final var result = getSrsCertificateFromCertificateService.getSrsCertificate("cert-1");

    assertNull(result);
  }

  @Test
  void shouldReturnSrsCertificateWhenCertificateIsAvailable() {
    final var signingDateTime = LocalDateTime.of(2025, 10, 30, 10, 30);
    final var sickLeaveCert = SickLeaveCertificateDTO.builder()
        .id("cert-1")
        .diagnoseCode("F438A")
        .signingDateTime(signingDateTime)
        .extendsCertificateId("parent-cert-1")
        .build();

    final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
        .available(true)
        .sickLeaveCertificate(sickLeaveCert)
        .build();

    when(csIntegrationService.getSickLeaveCertificate("cert-1")).thenReturn(Optional.of(response));

    final var result = getSrsCertificateFromCertificateService.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("cert-1", result.getCertificateId());
    assertEquals("F438A", result.getMainDiagnosisCode());
    assertEquals(signingDateTime.toLocalDate(), result.getSignedDate());
    assertEquals("parent-cert-1", result.getExtendsCertificateId());
  }

  @Test
  void shouldReturnSrsCertificateWithNullSigningDateTimeWhenNotProvided() {
    final var sickLeaveCert = SickLeaveCertificateDTO.builder()
        .id("cert-1")
        .diagnoseCode("F438A")
        .signingDateTime(null)
        .extendsCertificateId("parent-cert-1")
        .build();

    final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
        .available(true)
        .sickLeaveCertificate(sickLeaveCert)
        .build();

    when(csIntegrationService.getSickLeaveCertificate("cert-1")).thenReturn(Optional.of(response));

    final var result = getSrsCertificateFromCertificateService.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("cert-1", result.getCertificateId());
    assertEquals("F438A", result.getMainDiagnosisCode());
    assertNull(result.getSignedDate());
    assertEquals("parent-cert-1", result.getExtendsCertificateId());
  }

  @Test
  void shouldReturnSrsCertificateWithNullExtendsCertificateIdWhenNotProvided() {
    final var signingDateTime = LocalDateTime.of(2025, 10, 30, 10, 30);
    SickLeaveCertificateDTO sickLeaveCert = SickLeaveCertificateDTO.builder()
        .id("cert-1")
        .diagnoseCode("F438A")
        .signingDateTime(signingDateTime)
        .extendsCertificateId(null)
        .build();

    final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
        .available(true)
        .sickLeaveCertificate(sickLeaveCert)
        .build();

    when(csIntegrationService.getSickLeaveCertificate("cert-1")).thenReturn(Optional.of(response));

    final var result = getSrsCertificateFromCertificateService.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("cert-1", result.getCertificateId());
    assertEquals("F438A", result.getMainDiagnosisCode());
    assertEquals(signingDateTime.toLocalDate(), result.getSignedDate());
    assertNull(result.getExtendsCertificateId());
  }

  @Test
  void shouldReturnSrsCertificateWithAllNullableFieldsNull() {
    final var sickLeaveCert = SickLeaveCertificateDTO.builder()
        .id("cert-1")
        .diagnoseCode("F438A")
        .signingDateTime(null)
        .extendsCertificateId(null)
        .build();

    final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
        .available(true)
        .sickLeaveCertificate(sickLeaveCert)
        .build();

    when(csIntegrationService.getSickLeaveCertificate("cert-1")).thenReturn(Optional.of(response));

    final var result = getSrsCertificateFromCertificateService.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("cert-1", result.getCertificateId());
    assertEquals("F438A", result.getMainDiagnosisCode());
    assertNull(result.getSignedDate());
    assertNull(result.getExtendsCertificateId());
  }

}