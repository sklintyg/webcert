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
package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.webcert.print.CertificatePrintService;
import se.inera.intyg.webcert.print.api.PrintException;
import se.inera.intyg.webcert.print.api.PrintRequest;
import se.inera.intyg.webcert.print.api.PrintResult;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.RequiredFieldsForCertificatePdf;

@ExtendWith(MockitoExtension.class)
class GetGetCertificatePdfServiceTest {

  private static final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";
  private static final String CERTIFICATE_TYPE = "certificateType";
  private static final String CERTIFICATE_ID = "certificateId";
  private static final String WRONG_CUSTOMIZAITON_ID = "wrongCustomizaitonId";
  private static final String CUSTOMIZE_ID = "!diagnoser";
  private static final String INTERNAL_JSON_MODEL = "internalJsonModel";
  private static final List<Status> STATUSES = List.of(new Status());
  private static final RequiredFieldsForCertificatePdf REQUIRED_FIELDS_FOR_CERTIFICATE_PDF =
      RequiredFieldsForCertificatePdf.create(
          CERTIFICATE_TYPE_VERSION,
          CERTIFICATE_TYPE,
          INTERNAL_JSON_MODEL,
          STATUSES,
          UtkastStatus.SIGNED);
  private static final byte[] BYTES = "pdfData".getBytes(StandardCharsets.UTF_8);
  private static final String FILE_NAME = "fileName";
  private static final PrintResult PRINT_RESULT = new PrintResult(FILE_NAME, BYTES);
  private static final CertificatePdfResponseDTO EXPECTED_CERTIFICATE_PDF_RESPONSE =
      CertificatePdfResponseDTO.create(FILE_NAME, BYTES);

  @Mock private GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService;
  @Mock private CertificatePrintService certificatePrintService;
  @Captor private ArgumentCaptor<PrintRequest> printRequestCaptor;
  @InjectMocks private GetGetCertificatePdfService getCertificatePdfService;

  private PrintRequest printedRequest() {
    verify(certificatePrintService).print(printRequestCaptor.capture());
    return printRequestCaptor.getValue();
  }

  @Test
  void shouldReturnPdfResponse() {
    when(getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID))
        .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
    when(certificatePrintService.print(any(PrintRequest.class))).thenReturn(PRINT_RESULT);
    final var result = getCertificatePdfService.get(null, CERTIFICATE_ID, null);
    assertEquals(EXPECTED_CERTIFICATE_PDF_RESPONSE, result);
  }

  @Test
  void shouldThrowIllegalStateExceptionWithPrintExceptionAsCauseIfPrintFails() {
    final var printException = new PrintException("print failed", new RuntimeException());
    when(getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID))
        .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
    when(certificatePrintService.print(any(PrintRequest.class))).thenThrow(printException);

    final var exception =
        assertThrows(
            IllegalStateException.class,
            () -> getCertificatePdfService.get(null, CERTIFICATE_ID, null));

    assertSame(printException, exception.getCause());
    assertEquals("Unable to get pdf from module api implementation", exception.getMessage());
  }

  @Nested
  class PrintRequestTypeAndVersion {

    @BeforeEach
    void setUp() {
      when(getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID))
          .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
      when(certificatePrintService.print(any(PrintRequest.class))).thenReturn(PRINT_RESULT);
    }

    @Test
    void shouldUseTypeFromRequiredFields() {
      getCertificatePdfService.get(null, CERTIFICATE_ID, null);
      assertEquals(CERTIFICATE_TYPE, printedRequest().certificateType());
    }

    @Test
    void shouldUseTypeVersionFromRequiredFields() {
      getCertificatePdfService.get(null, CERTIFICATE_ID, null);
      assertEquals(CERTIFICATE_TYPE_VERSION, printedRequest().version());
    }
  }

  @Nested
  class GetRequiredFieldsInteraction {

    private final ArgumentCaptor<String> stringArgumentCaptor =
        ArgumentCaptor.forClass(String.class);

    @Test
    void shouldUseProvidedCertificateId() {
      when(getRequiredFieldsForCertificatePdfService.get(stringArgumentCaptor.capture()))
          .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
      when(certificatePrintService.print(any(PrintRequest.class))).thenReturn(PRINT_RESULT);
      getCertificatePdfService.get(null, CERTIFICATE_ID, null);

      assertEquals(CERTIFICATE_ID, stringArgumentCaptor.getValue());
    }
  }

  @Nested
  class PrintRequestInteraction {

    @BeforeEach
    void setUp() {
      when(getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID))
          .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
      when(certificatePrintService.print(any(PrintRequest.class))).thenReturn(PRINT_RESULT);
    }

    @Test
    void shouldUseJsonModelFromRequiredFields() {
      getCertificatePdfService.get(null, CERTIFICATE_ID, null);
      assertEquals(INTERNAL_JSON_MODEL, printedRequest().jsonModel());
    }

    @Test
    void shouldUseStatusesFromRequiredFields() {
      getCertificatePdfService.get(null, CERTIFICATE_ID, null);
      assertEquals(STATUSES, printedRequest().statuses());
    }

    @Test
    void shouldUseStatusFromRequiredFields() {
      getCertificatePdfService.get(null, CERTIFICATE_ID, null);
      assertEquals(UtkastStatus.SIGNED, printedRequest().utkastStatus());
    }

    @Test
    void shouldUseOriginMinaIntyg() {
      getCertificatePdfService.get(null, CERTIFICATE_ID, null);
      assertEquals(ApplicationOrigin.MINA_INTYG, printedRequest().origin());
    }

    @Test
    void shouldRequestEmployerCopyWithOptionalFieldsIfCorrectCustomizeIdWasProvidedFromRequest() {
      getCertificatePdfService.get(CUSTOMIZE_ID, CERTIFICATE_ID, null);
      final var request = printedRequest();
      assertTrue(request.employerCopy());
      assertEquals(List.of("!diagnoser", "!onskarFormedlaDiagnos"), request.optionalFields());
    }

    @Test
    void shouldNotRequestEmployerCopyIfWrongCustomizeIdWasProvidedFromRequest() {
      getCertificatePdfService.get(WRONG_CUSTOMIZAITON_ID, CERTIFICATE_ID, null);
      final var request = printedRequest();
      assertFalse(request.employerCopy());
      assertEquals(List.of(), request.optionalFields());
    }

    @Test
    void shouldNotRequestEmployerCopyIfNoCustomizeIdWasProvidedFromRequest() {
      getCertificatePdfService.get(null, CERTIFICATE_ID, null);
      final var request = printedRequest();
      assertFalse(request.employerCopy());
      assertEquals(List.of(), request.optionalFields());
    }
  }
}
