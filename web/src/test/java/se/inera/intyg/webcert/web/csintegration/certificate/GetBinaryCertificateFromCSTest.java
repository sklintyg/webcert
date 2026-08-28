/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateInternalPdfResponseDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateMetadataDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.GetBinaryCertificateResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetBinaryCertificateFromCSTest {

  private static final String CERTIFICATE_ID = "certificateId";
  private static final byte[] PDF_DATA = "pdfData".getBytes();
  private static final BinaryCertificateMetadataDTO METADATA =
      BinaryCertificateMetadataDTO.builder().certificateId(CERTIFICATE_ID).build();

  @Mock private CSIntegrationService csIntegrationService;

  @InjectMocks private GetBinaryCertificateFromCS getBinaryCertificateFromCS;

  @Test
  void shouldReturnNullIfCertificateDoesNotExistInCertificateService() {
    when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(false);

    assertNull(getBinaryCertificateFromCS.get(CERTIFICATE_ID));
  }

  @Test
  void shouldUseCertificateIdWhenCheckingIfCertificateExists() {
    when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(false);

    getBinaryCertificateFromCS.get(CERTIFICATE_ID);

    verify(csIntegrationService).certificateExists(CERTIFICATE_ID);
  }

  @Test
  void shouldReturnGetBinaryCertificateResponseWhenCertificateExistsInCertificateService() {
    final var expectedResponse =
        GetBinaryCertificateResponseDTO.builder().pdfData(PDF_DATA).metadata(METADATA).build();

    when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
    when(csIntegrationService.getBinaryCertificate(CERTIFICATE_ID))
        .thenReturn(
            GetCertificateInternalPdfResponseDTO.builder()
                .pdfData(PDF_DATA)
                .metadata(METADATA)
                .build());

    final var actualResponse = getBinaryCertificateFromCS.get(CERTIFICATE_ID);

    assertEquals(expectedResponse, actualResponse);
  }
}
