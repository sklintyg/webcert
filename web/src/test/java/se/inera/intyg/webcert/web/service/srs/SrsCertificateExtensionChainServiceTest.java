package se.inera.intyg.webcert.web.service.srs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;

@ExtendWith(MockitoExtension.class)
class SrsCertificateExtensionChainServiceTest {

  private static final String CERTIFICATE_ID = "certificateId";

  @Mock
  private GetSrsCertificateAggregator getSrsCertificateAggregator;

  @InjectMocks
  SrsCertificateExtensionChainService srsCertificateExtensionChainService;

  @Test
  void shouldReturnSingleCertificateWhenNoExtension() {
    when(getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID))
        .thenReturn(new SrsCertificate(null, null, null, null));

    final var result = srsCertificateExtensionChainService.get(CERTIFICATE_ID);
    assertEquals(1, result.size());
  }

  @Test
  void shouldReturnEmptyListWhenCertificateIdIsNull() {
    final var result = srsCertificateExtensionChainService.get(null);
    assertEquals(0, result.size());
  }

  @Test
  void shouldReturnChainWithTwoCertificatesWhenOneExtension() {
    final var extendedCertificateId = "extendedCertificateId";
    final var extendedCertificate = new SrsCertificate(null, null, null, null);
    final var baseCertificate = new SrsCertificate(null, null, null, extendedCertificateId);

    when(getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID))
        .thenReturn(baseCertificate);
    when(getSrsCertificateAggregator.getSrsCertificate(extendedCertificateId))
        .thenReturn(extendedCertificate);

    final var result = srsCertificateExtensionChainService.get(CERTIFICATE_ID);

    assertEquals(2, result.size());
    assertEquals(baseCertificate, result.get(0));
    assertEquals(extendedCertificate, result.get(1));
  }

  @Test
  void shouldReturnChainWithMultipleCertificatesWhenMultipleExtensions() {
    final var extendedCertificateId1 = "extendedCertificateId1";
    final var extendedCertificateId2 = "extendedCertificateId2";
    final var extendedCertificateId3 = "extendedCertificateId3";

    final var certificate3 = new SrsCertificate(null, null, null, null);
    final var certificate2 = new SrsCertificate(null, null, null, extendedCertificateId3);
    final var certificate1 = new SrsCertificate(null, null, null, extendedCertificateId2);
    final var baseCertificate = new SrsCertificate(null, null, null, extendedCertificateId1);

    when(getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID))
        .thenReturn(baseCertificate);
    when(getSrsCertificateAggregator.getSrsCertificate(extendedCertificateId1))
        .thenReturn(certificate1);
    when(getSrsCertificateAggregator.getSrsCertificate(extendedCertificateId2))
        .thenReturn(certificate2);
    when(getSrsCertificateAggregator.getSrsCertificate(extendedCertificateId3))
        .thenReturn(certificate3);

    final var result = srsCertificateExtensionChainService.get(CERTIFICATE_ID);

    assertEquals(4, result.size());
    assertEquals(baseCertificate, result.get(0));
    assertEquals(certificate1, result.get(1));
    assertEquals(certificate2, result.get(2));
    assertEquals(certificate3, result.get(3));
  }

  @Test
  void shouldReturnCorrectOrderInChain() {
    final var extendedCertificateId = "extendedCertificateId";
    final var extendedCertificate = new SrsCertificate("extended", null, null, null);
    final var baseCertificate = new SrsCertificate("base", null, null, extendedCertificateId);

    when(getSrsCertificateAggregator.getSrsCertificate(CERTIFICATE_ID))
        .thenReturn(baseCertificate);
    when(getSrsCertificateAggregator.getSrsCertificate(extendedCertificateId))
        .thenReturn(extendedCertificate);

    final var result = srsCertificateExtensionChainService.get(CERTIFICATE_ID);

    assertEquals(2, result.size());
    assertEquals("base", result.get(0).getCertificateId());
    assertEquals("extended", result.get(1).getCertificateId());
  }

}