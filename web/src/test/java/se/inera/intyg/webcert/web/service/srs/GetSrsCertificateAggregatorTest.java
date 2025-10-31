package se.inera.intyg.webcert.web.service.srs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@ExtendWith(MockitoExtension.class)
class GetSrsCertificateAggregatorTest {

  private static final String CERTIFICATE_ID = "certificateId";

  @Mock
  private CSIntegrationService csIntegrationService;
  @Mock
  private GetSrsCertificate getSrsCertificateFromWC;
  @Mock
  private GetSrsCertificate getSrsCertificateFromCS;

  private GetSrsCertificateAggregator getSrsCertificateAggregator;

  @BeforeEach
  void setUp() {
    getSrsCertificateAggregator = new GetSrsCertificateAggregator(
        csIntegrationService,
        getSrsCertificateFromWC,
        getSrsCertificateFromCS
    );
  }

  @Test
  void shouldGetSrsCertificateFromCSWhenCertificateExistsInCS() {
    final var expectedCertificate = new SrsCertificate("csId", null, null, null);

    when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
    when(getSrsCertificateFromCS.getSrsCertificate(CERTIFICATE_ID)).thenReturn(expectedCertificate);

    final var result = getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID);

    assertEquals(expectedCertificate, result);
    verify(csIntegrationService).certificateExists(CERTIFICATE_ID);
    verify(getSrsCertificateFromCS).getSrsCertificate(CERTIFICATE_ID);
    verifyNoInteractions(getSrsCertificateFromWC);
  }

  @Test
  void shouldGetSrsCertificateFromWCWhenCertificateDoesNotExistInCS() {
    final var expectedCertificate = new SrsCertificate("wcId", null, null, null);

    when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(false);
    when(getSrsCertificateFromWC.getSrsCertificate(CERTIFICATE_ID)).thenReturn(expectedCertificate);

    final var result = getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID);

    assertEquals(expectedCertificate, result);
    verify(csIntegrationService).certificateExists(CERTIFICATE_ID);
    verify(getSrsCertificateFromWC).getSrsCertificate(CERTIFICATE_ID);
    verifyNoInteractions(getSrsCertificateFromCS);
  }

  @Test
  void shouldCheckCSIntegrationServiceBeforeGettingCertificate() {
    when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
    when(getSrsCertificateFromCS.getSrsCertificate(CERTIFICATE_ID))
        .thenReturn(new SrsCertificate(null, null, null, null));

    getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID);

    verify(csIntegrationService).certificateExists(CERTIFICATE_ID);
  }

  @Test
  void shouldReturnCertificateWithAllFieldsFromCS() {
    final var certificateId = "cert-123";
    final var diagnosis = "diagnosis-code";
    final var extension = "extension-id";
    final var expectedCertificate = new SrsCertificate(certificateId, diagnosis, null, extension);

    when(csIntegrationService.certificateExists(certificateId)).thenReturn(true);
    when(getSrsCertificateFromCS.getSrsCertificate(certificateId)).thenReturn(expectedCertificate);

    final var result = getSrsCertificateAggregator.getSrsCertificate(certificateId);

    assertEquals(certificateId, result.getCertificateId());
    assertEquals(diagnosis, result.getMainDiagnosisCode());
    assertNull(result.getSignedDate());
    assertEquals(extension, result.getExtendsCertificateId());
  }

  @Test
  void shouldReturnCertificateWithAllFieldsFromWC() {
    final var certificateId = "cert-456";
    final var diagnosis = "diagnosis-code-wc";
    final var extension = "extension-id-wc";
    final var expectedCertificate = new SrsCertificate(certificateId, diagnosis, null, extension);

    when(csIntegrationService.certificateExists(certificateId)).thenReturn(false);
    when(getSrsCertificateFromWC.getSrsCertificate(certificateId)).thenReturn(expectedCertificate);

    final var result = getSrsCertificateAggregator.getSrsCertificate(certificateId);

    assertEquals(certificateId, result.getCertificateId());
    assertEquals(diagnosis, result.getMainDiagnosisCode());
    assertNull(result.getSignedDate());
    assertEquals(extension, result.getExtendsCertificateId());
  }

  @Test
  void shouldOnlyCallCSWhenCertificateExistsInCS() {
    final var expectedCertificate = new SrsCertificate(CERTIFICATE_ID, null, null, null);

    when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
    when(getSrsCertificateFromCS.getSrsCertificate(CERTIFICATE_ID)).thenReturn(expectedCertificate);

    getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID);

    verify(getSrsCertificateFromCS).getSrsCertificate(CERTIFICATE_ID);
    verifyNoInteractions(getSrsCertificateFromWC);
  }

  @Test
  void shouldOnlyCallWCWhenCertificateDoesNotExistInCS() {
    final var expectedCertificate = new SrsCertificate(CERTIFICATE_ID, null, null, null);

    when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(false);
    when(getSrsCertificateFromWC.getSrsCertificate(CERTIFICATE_ID)).thenReturn(expectedCertificate);

    getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID);

    verify(getSrsCertificateFromWC).getSrsCertificate(CERTIFICATE_ID);
    verifyNoInteractions(getSrsCertificateFromCS);
  }

  @Test
  void shouldHandleDifferentCertificateIdsForMultipleCalls() {
    final var certificateId1 = "cert-1";
    final var certificateId2 = "cert-2";
    final var certificate1 = new SrsCertificate(certificateId1, null, null, null);
    final var certificate2 = new SrsCertificate(certificateId2, null, null, null);

    when(csIntegrationService.certificateExists(certificateId1)).thenReturn(true);
    when(csIntegrationService.certificateExists(certificateId2)).thenReturn(false);
    when(getSrsCertificateFromCS.getSrsCertificate(certificateId1)).thenReturn(certificate1);
    when(getSrsCertificateFromWC.getSrsCertificate(certificateId2)).thenReturn(certificate2);

    final var result1 = getSrsCertificateAggregator.getSrsCertificate(certificateId1);
    final var result2 = getSrsCertificateAggregator.getSrsCertificate(certificateId2);

    assertEquals(certificateId1, result1.getCertificateId());
    assertEquals(certificateId2, result2.getCertificateId());
    verify(getSrsCertificateFromCS).getSrsCertificate(certificateId1);
    verify(getSrsCertificateFromWC).getSrsCertificate(certificateId2);
  }
}