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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.RequiredFieldsForCertificatePdf;

@ExtendWith(MockitoExtension.class)
class GetBinaryCertificateFromWCTest {

  private static final String CERTIFICATE_ID = "e5f6a1b2-3c4d-4e5f-8a1b-2c3d4e5f6a1b";
  private static final String CERTIFICATE_TYPE_VERSION = "1.3";
  private static final String CERTIFICATE_TYPE = "lisjp";
  private static final String INTERNAL_JSON_MODEL = "{\"id\":\"" + CERTIFICATE_ID + "\"}";
  private static final List<Status> STATUSES =
      List.of(new Status(CertificateState.RECEIVED, "FKASSA", LocalDateTime.now()));
  private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};
  private static final RequiredFieldsForCertificatePdf REQUIRED_FIELDS_FOR_CERTIFICATE_PDF =
      RequiredFieldsForCertificatePdf.create(
          CERTIFICATE_TYPE_VERSION,
          CERTIFICATE_TYPE,
          INTERNAL_JSON_MODEL,
          STATUSES,
          UtkastStatus.SIGNED);
  private static final PdfResponse PDF_RESPONSE = new PdfResponse(PDF_BYTES, "fileName");

  @Mock private GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService;
  @Mock private IntygModuleRegistryImpl moduleRegistry;
  @Mock private ModuleApi moduleApi;

  @InjectMocks private GetBinaryCertificateFromWC getBinaryCertificateFromWC;

  @BeforeEach
  void setUp() throws ModuleNotFoundException {
    when(getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID))
        .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
    when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION))
        .thenReturn(moduleApi);
  }

  @Test
  void shouldReturnPdfData() throws ModuleException {
    when(moduleApi.pdf(
            INTERNAL_JSON_MODEL, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED))
        .thenReturn(PDF_RESPONSE);

    final var response = getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    assertEquals(PDF_BYTES, response.getPdfData());
  }

  @Test
  void shouldUseCertificateIdWhenGettingRequiredFields() throws ModuleException {
    when(moduleApi.pdf(
            INTERNAL_JSON_MODEL, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED))
        .thenReturn(PDF_RESPONSE);

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(getRequiredFieldsForCertificatePdfService).get(CERTIFICATE_ID);
  }

  @Test
  void shouldUseCertificateTypeAndVersionFromRequiredFieldsWhenGettingModuleApi()
      throws ModuleNotFoundException, ModuleException {
    when(moduleApi.pdf(
            INTERNAL_JSON_MODEL, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED))
        .thenReturn(PDF_RESPONSE);

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(moduleRegistry).getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
  }

  @Test
  void shouldUseJsonModelStatusesOriginAndStatusFromRequiredFieldsWhenGeneratingPdf()
      throws ModuleException {
    when(moduleApi.pdf(
            INTERNAL_JSON_MODEL, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED))
        .thenReturn(PDF_RESPONSE);

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(moduleApi)
        .pdf(INTERNAL_JSON_MODEL, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED);
  }

  @Test
  void shouldThrowIllegalStateExceptionIfModuleNotFound() throws ModuleNotFoundException {
    when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION))
        .thenThrow(new ModuleNotFoundException("Module not found"));

    assertThrows(IllegalStateException.class, () -> getBinaryCertificateFromWC.get(CERTIFICATE_ID));
  }

  @Test
  void shouldThrowIllegalStateExceptionIfModuleApiThrowsModuleException() throws ModuleException {
    when(moduleApi.pdf(
            INTERNAL_JSON_MODEL, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED))
        .thenThrow(new ModuleException("Failed to generate pdf"));

    assertThrows(IllegalStateException.class, () -> getBinaryCertificateFromWC.get(CERTIFICATE_ID));
  }
}
