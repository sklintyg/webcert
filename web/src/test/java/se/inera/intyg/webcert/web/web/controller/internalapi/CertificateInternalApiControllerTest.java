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
package se.inera.intyg.webcert.web.web.controller.internalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetCertificateInternalAggregator;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetGetCertificateInternalPdfAggregator;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.GetBinaryCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateIntegrationRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@ExtendWith(MockitoExtension.class)
class CertificateInternalApiControllerTest {

  private static final byte[] EXPECTED_PDF_DATA = new byte[0];
  private static final String EXPECTED_FILENAME = "filename";
  private static final CertificatePdfResponseDTO EXPECTED_PDL_RESPONSE =
      CertificatePdfResponseDTO.create(EXPECTED_FILENAME, EXPECTED_PDF_DATA);
  private static final String CERTIFICATE_ID = "certificateId";
  private static final String CUSTOMIZATION_ID = "customizationId";
  private static final String PERSON_ID = "personId";
  @Mock private GetBinaryCertificate getBinaryCertificate;
  @Mock private GetGetCertificateInternalPdfAggregator getCertificateInternalPdfAggregator;

  @Mock private GetCertificateInternalAggregator certificateInternalAggregator;

  @Mock private WebCertServiceExceptionResponseMapper webCertServiceExceptionResponseMapper;

  @InjectMocks private CertificateInternalApiController certificateInternalApiController;

  @Nested
  class GetCertificate {

    private static final String PERSON_ID = "personId";

    @Test
    void shallReturnGetCertificateResponse() {
      final var expectedResponse = GetCertificateResponse.create(new Certificate());
      final var getCertificateIntegrationRequestDTO =
          GetCertificateIntegrationRequestDTO.builder().personId(PERSON_ID).build();

      doReturn(expectedResponse).when(certificateInternalAggregator).get(CERTIFICATE_ID, PERSON_ID);

      final var getCertificateResponse =
          certificateInternalApiController.getCertificate(
              getCertificateIntegrationRequestDTO, CERTIFICATE_ID);

      assertEquals(expectedResponse, getCertificateResponse);
    }
  }

  @Nested
  class GetPdfData {

    private final CertificatePdfRequestDTO printCertificateRequest =
        CertificatePdfRequestDTO.builder()
            .customizationId(CUSTOMIZATION_ID)
            .personId(PERSON_ID)
            .build();

    @BeforeEach
    void setUp() {
      doReturn(EXPECTED_PDL_RESPONSE)
          .when(getCertificateInternalPdfAggregator)
          .get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);
    }

    @Test
    void shallReturnPrintCertificateResponseWithFileName() {
      final var actualPrintCertificateResponse =
          certificateInternalApiController.getPdfData(printCertificateRequest, CERTIFICATE_ID);
      assertEquals(EXPECTED_FILENAME, actualPrintCertificateResponse.getFilename());
    }

    @Test
    void shallReturnPrintCertificateResponseWithPdfData() {
      final var actualPrintCertificateResponse =
          certificateInternalApiController.getPdfData(printCertificateRequest, CERTIFICATE_ID);
      assertEquals(EXPECTED_PDF_DATA, actualPrintCertificateResponse.getPdfData());
    }
  }

  @Nested
  class GetBinaryCertificateTests {

    private static final GetBinaryCertificateResponseDTO EXPECTED_BINARY_CERTIFICATE_RESPONSE =
        GetBinaryCertificateResponseDTO.builder().build();

    @Test
    void shallReturnGetBinaryCertificateResponse() {
      when(getBinaryCertificate.get(CERTIFICATE_ID))
          .thenReturn(EXPECTED_BINARY_CERTIFICATE_RESPONSE);

      final var response =
          certificateInternalApiController.getBinaryCertificateData(CERTIFICATE_ID);

      assertEquals(EXPECTED_BINARY_CERTIFICATE_RESPONSE, response.getBody());
    }

    @Test
    void shallReturnNotFoundWhenCertificateIsMissing() {
      when(getBinaryCertificate.get(CERTIFICATE_ID)).thenReturn(null);

      final var response =
          certificateInternalApiController.getBinaryCertificateData(CERTIFICATE_ID);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shallUseCertificateIdFromPathVariable() {
      when(getBinaryCertificate.get(CERTIFICATE_ID))
          .thenReturn(EXPECTED_BINARY_CERTIFICATE_RESPONSE);

      certificateInternalApiController.getBinaryCertificateData(CERTIFICATE_ID);

      verify(getBinaryCertificate).get(CERTIFICATE_ID);
    }

    @Test
    void shallDelegateWebCertServiceExceptionToResponseMapper() {
      final var exception =
          new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "not found");
      final ResponseEntity<GetBinaryCertificateResponseDTO> expectedResponse =
          ResponseEntity.notFound().build();
      doThrow(exception).when(getBinaryCertificate).get(CERTIFICATE_ID);
      when(webCertServiceExceptionResponseMapper.<GetBinaryCertificateResponseDTO>toResponseEntity(
              exception))
          .thenReturn(expectedResponse);

      final var response =
          certificateInternalApiController.getBinaryCertificateData(CERTIFICATE_ID);

      assertEquals(expectedResponse, response);
    }
  }
}
